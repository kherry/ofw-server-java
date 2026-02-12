#!/bin/bash

# OFW Kubernetes Deployment Script
# This script helps deploy OFW server to Kubernetes

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl not found. Please install kubectl first."
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        print_error "docker not found. Please install Docker first."
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Build Docker image
build_image() {
    print_info "Building Docker image..."
    
    cd ..
    docker build -t ofw-server:latest .
    
    print_success "Docker image built successfully"
}

# Tag and push image
push_image() {
    local registry=$1
    
    if [ -z "$registry" ]; then
        print_info "No registry specified, skipping push"
        return
    fi
    
    print_info "Tagging and pushing image to $registry..."
    
    docker tag ofw-server:latest $registry/ofw-server:latest
    docker push $registry/ofw-server:latest
    
    print_success "Image pushed to registry"
}

# Deploy to Kubernetes
deploy() {
    print_info "Deploying to Kubernetes..."
    
    # Create namespace
    kubectl apply -f 00-namespace.yaml
    print_success "Namespace created"
    
    # Deploy MySQL
    kubectl apply -f 01-mysql-configmap.yaml
    kubectl apply -f 02-mysql-secret.yaml
    kubectl apply -f 03-mysql-pvc.yaml
    kubectl apply -f 04-mysql-statefulset.yaml
    kubectl apply -f 05-mysql-service.yaml
    print_success "MySQL deployed"
    
    # Wait for MySQL to be ready
    print_info "Waiting for MySQL to be ready..."
    kubectl wait --for=condition=ready pod -l app=mysql -n ofw --timeout=300s
    print_success "MySQL is ready"
    
    # Deploy OFW Server
    kubectl apply -f 06-ofw-server-configmap.yaml
    kubectl apply -f 07-ofw-server-deployment.yaml
    kubectl apply -f 08-ofw-server-service.yaml
    print_success "OFW Server deployed"
    
    # Wait for OFW Server to be ready
    print_info "Waiting for OFW Server to be ready..."
    kubectl wait --for=condition=available deployment/ofw-server -n ofw --timeout=300s
    print_success "OFW Server is ready"
    
    # Optional: Deploy Ingress and HPA
    read -p "Deploy Ingress? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        kubectl apply -f 09-ofw-server-ingress.yaml
        print_success "Ingress deployed"
    fi
    
    read -p "Deploy HorizontalPodAutoscaler? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        kubectl apply -f 10-ofw-server-hpa.yaml
        print_success "HPA deployed"
    fi
}

# Show deployment status
show_status() {
    print_info "Deployment Status:"
    echo
    
    echo "=== Pods ==="
    kubectl get pods -n ofw
    echo
    
    echo "=== Services ==="
    kubectl get svc -n ofw
    echo
    
    echo "=== Deployments ==="
    kubectl get deployment -n ofw
    echo
    
    echo "=== StatefulSets ==="
    kubectl get statefulset -n ofw
    echo
}

# Get service URL
get_url() {
    print_info "Getting service URL..."
    
    SERVICE_TYPE=$(kubectl get svc ofw-server -n ofw -o jsonpath='{.spec.type}')
    
    if [ "$SERVICE_TYPE" = "LoadBalancer" ]; then
        EXTERNAL_IP=$(kubectl get svc ofw-server -n ofw -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
        
        if [ -z "$EXTERNAL_IP" ]; then
            EXTERNAL_IP=$(kubectl get svc ofw-server -n ofw -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
        fi
        
        if [ -z "$EXTERNAL_IP" ]; then
            print_info "LoadBalancer IP pending..."
            print_info "Run: kubectl get svc ofw-server -n ofw -w"
        else
            print_success "Service URL: http://$EXTERNAL_IP/api/v1/upload/health"
        fi
    elif [ "$SERVICE_TYPE" = "NodePort" ]; then
        NODE_PORT=$(kubectl get svc ofw-server -n ofw -o jsonpath='{.spec.ports[0].nodePort}')
        NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
        
        if [ -z "$NODE_IP" ]; then
            NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
        fi
        
        print_success "Service URL: http://$NODE_IP:$NODE_PORT/api/v1/upload/health"
    else
        print_info "Service is ClusterIP. Use port-forward to access:"
        print_info "kubectl port-forward -n ofw svc/ofw-server 8080:80"
        print_info "Then access: http://localhost:8080/api/v1/upload/health"
    fi
}

# Test the deployment
test_deployment() {
    print_info "Testing deployment..."
    
    # Port forward for testing
    kubectl port-forward -n ofw svc/ofw-server 8080:80 &
    PF_PID=$!
    
    sleep 5
    
    # Test health endpoint
    if curl -s http://localhost:8080/api/v1/upload/health > /dev/null; then
        print_success "Health check passed!"
    else
        print_error "Health check failed!"
    fi
    
    # Kill port-forward
    kill $PF_PID 2>/dev/null
}

# Main menu
main_menu() {
    echo "=================================="
    echo "OFW Kubernetes Deployment"
    echo "=================================="
    echo
    echo "1. Build Docker image"
    echo "2. Push to registry"
    echo "3. Deploy to Kubernetes"
    echo "4. Show status"
    echo "5. Get service URL"
    echo "6. Test deployment"
    echo "7. Full deployment (build + push + deploy)"
    echo "8. Clean up (delete all resources)"
    echo "9. Exit"
    echo
    read -p "Select option: " choice
    
    case $choice in
        1)
            build_image
            ;;
        2)
            read -p "Enter registry URL (e.g., docker.io/username): " registry
            push_image "$registry"
            ;;
        3)
            deploy
            show_status
            get_url
            ;;
        4)
            show_status
            ;;
        5)
            get_url
            ;;
        6)
            test_deployment
            ;;
        7)
            build_image
            read -p "Enter registry URL (leave empty to skip): " registry
            if [ ! -z "$registry" ]; then
                push_image "$registry"
            fi
            deploy
            show_status
            get_url
            test_deployment
            ;;
        8)
            read -p "Are you sure you want to delete all resources? (yes/no) " confirm
            if [ "$confirm" = "yes" ]; then
                kubectl delete namespace ofw
                print_success "All resources deleted"
            fi
            ;;
        9)
            exit 0
            ;;
        *)
            print_error "Invalid option"
            ;;
    esac
}

# Script entry point
cd "$(dirname "$0")"

check_prerequisites

while true; do
    main_menu
    echo
    read -p "Press Enter to continue..."
    clear
done
