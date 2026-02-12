# Kubernetes Deployment Guide for OFW Server

This guide covers deploying the OFW Server and MySQL to Kubernetes.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                       │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Namespace: ofw                           │ │
│  │                                                       │ │
│  │  ┌──────────────┐         ┌──────────────┐          │ │
│  │  │   Ingress    │         │ Load Balancer│          │ │
│  │  │ (optional)   │         │   Service    │          │ │
│  │  └──────┬───────┘         └──────┬───────┘          │ │
│  │         │                        │                   │ │
│  │  ┌──────▼────────────────────────▼───────┐          │ │
│  │  │      OFW Server Service               │          │ │
│  │  │      (ClusterIP/LoadBalancer)         │          │ │
│  │  └──────┬────────────────────────────────┘          │ │
│  │         │                                            │ │
│  │  ┌──────▼────────────────────────────────┐          │ │
│  │  │   OFW Server Deployment               │          │ │
│  │  │   (2-10 replicas with HPA)            │          │ │
│  │  │                                        │          │ │
│  │  │  ┌──────────┐      ┌──────────┐      │          │ │
│  │  │  │  Pod 1   │      │  Pod 2   │      │          │ │
│  │  │  │          │      │          │      │          │ │
│  │  │  └────┬─────┘      └────┬─────┘      │          │ │
│  │  └───────┼─────────────────┼────────────┘          │ │
│  │          │                 │                        │ │
│  │  ┌───────▼─────────────────▼────────────┐          │ │
│  │  │      MySQL Service (Headless)        │          │ │
│  │  └───────┬──────────────────────────────┘          │ │
│  │          │                                          │ │
│  │  ┌───────▼──────────────────────────────┐          │ │
│  │  │   MySQL StatefulSet (1 replica)      │          │ │
│  │  │                                       │          │ │
│  │  │  ┌──────────────────────────┐        │          │ │
│  │  │  │  mysql-0                 │        │          │ │
│  │  │  │                          │        │          │ │
│  │  │  │  ┌────────────────────┐ │        │          │ │
│  │  │  │  │ PersistentVolume   │ │        │          │ │
│  │  │  │  │   (10Gi storage)   │ │        │          │ │
│  │  │  │  └────────────────────┘ │        │          │ │
│  │  │  └──────────────────────────┘        │          │ │
│  │  └───────────────────────────────────────┘          │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Prerequisites

- Kubernetes cluster (1.20+)
- kubectl configured to access your cluster
- Docker for building images
- (Optional) Ingress controller for external access
- (Optional) cert-manager for HTTPS

## Files Overview

| File | Purpose |
|------|---------|
| `00-namespace.yaml` | Creates `ofw` namespace |
| `01-mysql-configmap.yaml` | MySQL initialization SQL |
| `02-mysql-secret.yaml` | MySQL credentials (change defaults!) |
| `03-mysql-pvc.yaml` | Persistent storage for MySQL |
| `04-mysql-statefulset.yaml` | MySQL database deployment |
| `05-mysql-service.yaml` | MySQL service (headless) |
| `06-ofw-server-configmap.yaml` | OFW server configuration |
| `07-ofw-server-deployment.yaml` | OFW server application |
| `08-ofw-server-service.yaml` | OFW server service |
| `09-ofw-server-ingress.yaml` | External access (optional) |
| `10-ofw-server-hpa.yaml` | Auto-scaling configuration |
| `kustomization.yaml` | Kustomize configuration |

## Quick Start

### 1. Build and Push Docker Image

```bash
# Build the image
cd ..
docker build -t ofw-server:latest .

# Tag for your registry
docker tag ofw-server:latest your-registry/ofw-server:latest

# Push to registry
docker push your-registry/ofw-server:latest
```

### 2. Update Configuration

Edit `02-mysql-secret.yaml` and change passwords:

```yaml
stringData:
  mysql-root-password: "YOUR_SECURE_ROOT_PASSWORD"
  mysql-password: "YOUR_SECURE_PASSWORD"
```

Edit `kustomization.yaml` and update image registry:

```yaml
images:
  - name: ofw-server
    newName: your-registry/ofw-server  # Your Docker registry
    newTag: latest
```

### 3. Deploy with kubectl

```bash
# Apply all manifests
kubectl apply -k k8s/

# Or apply individually
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-mysql-configmap.yaml
kubectl apply -f k8s/02-mysql-secret.yaml
kubectl apply -f k8s/03-mysql-pvc.yaml
kubectl apply -f k8s/04-mysql-statefulset.yaml
kubectl apply -f k8s/05-mysql-service.yaml
kubectl apply -f k8s/06-ofw-server-configmap.yaml
kubectl apply -f k8s/07-ofw-server-deployment.yaml
kubectl apply -f k8s/08-ofw-server-service.yaml
kubectl apply -f k8s/09-ofw-server-ingress.yaml
kubectl apply -f k8s/10-ofw-server-hpa.yaml
```

### 4. Verify Deployment

```bash
# Check namespace
kubectl get all -n ofw

# Check MySQL
kubectl get statefulset -n ofw
kubectl get pods -n ofw -l app=mysql

# Check OFW Server
kubectl get deployment -n ofw
kubectl get pods -n ofw -l app=ofw-server

# Check services
kubectl get svc -n ofw
```

### 5. Access the Application

**Via LoadBalancer:**
```bash
# Get external IP
kubectl get svc ofw-server -n ofw

# Access at http://<EXTERNAL-IP>/api/v1/upload/health
```

**Via Port Forward (for testing):**
```bash
kubectl port-forward -n ofw svc/ofw-server 8080:80

# Access at http://localhost:8080/api/v1/upload/health
```

**Via Ingress:**
```bash
# Get ingress address
kubectl get ingress -n ofw

# Access at http://ofw.example.com (or your configured domain)
```

## Scaling

### Manual Scaling

```bash
# Scale OFW server
kubectl scale deployment ofw-server -n ofw --replicas=5

# Note: MySQL is a StatefulSet with 1 replica (not designed for multi-replica)
```

### Auto-Scaling (HPA)

The HorizontalPodAutoscaler is configured to:
- Min replicas: 2
- Max replicas: 10
- Target CPU: 70%
- Target Memory: 80%

```bash
# Check HPA status
kubectl get hpa -n ofw

# View HPA details
kubectl describe hpa ofw-server-hpa -n ofw
```

## Monitoring

### Check Pod Logs

```bash
# OFW Server logs
kubectl logs -n ofw -l app=ofw-server --tail=100 -f

# MySQL logs
kubectl logs -n ofw mysql-0 --tail=100 -f
```

### Check Pod Status

```bash
# Detailed pod information
kubectl describe pod -n ofw <pod-name>

# Get pod events
kubectl get events -n ofw --sort-by='.lastTimestamp'
```

### Execute Commands in Pods

```bash
# Connect to MySQL
kubectl exec -it -n ofw mysql-0 -- mysql -u ofw_user -p ofw_db

# Check OFW server container
kubectl exec -it -n ofw <ofw-pod-name> -- /bin/sh
```

## Updating the Application

### Rolling Update

```bash
# Update image version in kustomization.yaml
# Then apply:
kubectl apply -k k8s/

# Or update deployment directly
kubectl set image deployment/ofw-server -n ofw \
  ofw-server=your-registry/ofw-server:v2.0.0

# Watch rollout
kubectl rollout status deployment/ofw-server -n ofw
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/ofw-server -n ofw

# Rollback to specific revision
kubectl rollout undo deployment/ofw-server -n ofw --to-revision=2

# View rollout history
kubectl rollout history deployment/ofw-server -n ofw
```

## Database Management

### Backup MySQL Data

```bash
# Create backup
kubectl exec -n ofw mysql-0 -- mysqldump -u root -p<password> ofw_db > backup.sql

# Or using a job (recommended)
cat <<EOF | kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: mysql-backup
  namespace: ofw
spec:
  template:
    spec:
      containers:
      - name: backup
        image: mysql:8.0
        command:
        - /bin/sh
        - -c
        - mysqldump -h mysql-0.mysql -u root -p\$MYSQL_ROOT_PASSWORD ofw_db > /backup/backup.sql
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: mysql-root-password
        volumeMounts:
        - name: backup
          mountPath: /backup
      restartPolicy: Never
      volumes:
      - name: backup
        emptyDir: {}
EOF
```

### Restore MySQL Data

```bash
# Copy backup to pod
kubectl cp backup.sql ofw/mysql-0:/tmp/backup.sql

# Restore
kubectl exec -n ofw mysql-0 -- mysql -u root -p<password> ofw_db < /tmp/backup.sql
```

## Troubleshooting

### MySQL Not Starting

```bash
# Check logs
kubectl logs -n ofw mysql-0

# Check PVC
kubectl get pvc -n ofw

# Check if PVC is bound
kubectl describe pvc mysql-pvc -n ofw
```

### OFW Server Can't Connect to MySQL

```bash
# Check service DNS
kubectl exec -n ofw <ofw-pod> -- nslookup mysql-0.mysql.ofw.svc.cluster.local

# Check MySQL service
kubectl get svc mysql -n ofw

# Verify init container logs
kubectl logs -n ofw <ofw-pod> -c wait-for-mysql
```

### Application Errors

```bash
# View application logs
kubectl logs -n ofw -l app=ofw-server --tail=200

# Check environment variables
kubectl exec -n ofw <ofw-pod> -- env | grep DB_

# Restart deployment
kubectl rollout restart deployment/ofw-server -n ofw
```

## Resource Management

### View Resource Usage

```bash
# Pod resource usage
kubectl top pods -n ofw

# Node resource usage
kubectl top nodes
```

### Adjust Resources

Edit deployment and update:

```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

Apply changes:
```bash
kubectl apply -f k8s/07-ofw-server-deployment.yaml
```

## Security Best Practices

1. **Change Default Passwords**
   - Update `02-mysql-secret.yaml` before deployment
   - Use strong, randomly generated passwords

2. **Use Private Registry**
   - Push images to private registry
   - Configure imagePullSecrets if needed

3. **Network Policies**
   - Restrict pod-to-pod communication
   - Allow only necessary traffic

4. **RBAC**
   - Create service accounts with minimal permissions
   - Use pod security policies

5. **Enable TLS**
   - Configure Ingress with TLS
   - Use cert-manager for automated certificates

## Production Considerations

1. **High Availability**
   - Run MySQL in a managed service (Cloud SQL, RDS)
   - Or use MySQL clustering/replication
   - Use multiple Kubernetes nodes

2. **Persistent Storage**
   - Use cloud provider storage classes (gp2, pd-ssd)
   - Configure backup/snapshot policies
   - Test restore procedures

3. **Monitoring**
   - Install Prometheus & Grafana
   - Configure alerting
   - Monitor application metrics

4. **Logging**
   - Use ELK stack or cloud logging
   - Aggregate logs from all pods
   - Set up log retention policies

5. **CI/CD**
   - Automate image builds
   - Use GitOps (ArgoCD, Flux)
   - Implement automated testing

## Clean Up

### Remove All Resources

```bash
# Delete with kustomize
kubectl delete -k k8s/

# Or delete namespace (removes everything)
kubectl delete namespace ofw
```

### Remove Persistent Data

```bash
# Delete PVC (this deletes data!)
kubectl delete pvc mysql-pvc -n ofw
```

## Support

For issues or questions:
- Check application logs
- Review Kubernetes events
- Consult main README.md
- Contact development team

## Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [MySQL on Kubernetes](https://kubernetes.io/docs/tutorials/stateful-application/mysql-wordpress-persistent-volume/)
- [Kustomize](https://kustomize.io/)
