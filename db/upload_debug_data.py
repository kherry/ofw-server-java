"""
Upload debug data to OFW Server

This script uploads JSON files from the debug directory to the OFW server.
"""

import requests
import os
import glob
import json
from pathlib import Path


def upload_debug_data(server_url="http://localhost:8080", 
                      debug_dir="debug",
                      user_id=None,
                      notes=None):
    """
    Upload debug data files to OFW server.
    
    Args:
        server_url: Base URL of OFW server
        debug_dir: Directory containing debug files
        user_id: User ID to associate with upload
        notes: Optional notes about the upload
    """
    endpoint = f"{server_url}/api/v1/upload/debug"
    
    # Find JSON files in debug directory
    json_files = glob.glob(os.path.join(debug_dir, "*.json"))
    
    if not json_files:
        print(f"No JSON files found in {debug_dir}")
        return
    
    print(f"Found {len(json_files)} JSON files")
    print("-" * 70)
    
    # Prepare files for upload
    files = []
    file_handles = []
    
    for json_file in json_files:
        filename = os.path.basename(json_file)
        print(f"  - {filename}")
        
        # Open file and add to list
        fh = open(json_file, 'rb')
        file_handles.append(fh)
        files.append(('files', (filename, fh, 'application/json')))
    
    # Prepare form data
    data = {}
    if user_id:
        data['userId'] = user_id
    if notes:
        data['notes'] = notes
    
    print("-" * 70)
    print(f"Uploading to {endpoint}...")
    print()
    
    try:
        # Upload files
        response = requests.post(endpoint, files=files, data=data)
        
        # Close file handles
        for fh in file_handles:
            fh.close()
        
        # Check response
        if response.status_code in [200, 206]:  # 200 OK or 206 Partial Content
            result = response.json()
            
            print("=" * 70)
            print("UPLOAD SUCCESSFUL")
            print("=" * 70)
            print(f"Session ID: {result['sessionId']}")
            print(f"Status: {result['status']}")
            print(f"Message: {result['message']}")
            print()
            print(f"Files processed: {result['filesProcessed']}")
            print(f"Records created: {result['recordsCreated']}")
            print(f"Errors: {result['errors']}")
            
            if result.get('errorMessages'):
                print()
                print("Error messages:")
                for error in result['errorMessages']:
                    print(f"  - {error}")
            
        else:
            print("=" * 70)
            print("UPLOAD FAILED")
            print("=" * 70)
            print(f"Status code: {response.status_code}")
            print(f"Response: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("=" * 70)
        print("CONNECTION ERROR")
        print("=" * 70)
        print(f"Could not connect to {server_url}")
        print()
        print("Make sure the OFW server is running:")
        print("  docker-compose up -d")
        print()
        print("Or check the server URL is correct")
        
    except Exception as e:
        print("=" * 70)
        print("ERROR")
        print("=" * 70)
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()


def main():
    print("=" * 70)
    print("OFW DEBUG DATA UPLOADER")
    print("=" * 70)
    print()
    
    # Configuration
    server_url = input("Server URL [http://localhost:8080]: ").strip()
    if not server_url:
        server_url = "http://localhost:8080"
    
    debug_dir = input("Debug directory [debug]: ").strip()
    if not debug_dir:
        debug_dir = "debug"
    
    user_id_str = input("User ID (optional, press Enter to skip): ").strip()
    user_id = int(user_id_str) if user_id_str else None
    
    notes = input("Notes (optional, press Enter to skip): ").strip()
    if not notes:
        notes = None
    
    print()
    
    # Upload
    upload_debug_data(
        server_url=server_url,
        debug_dir=debug_dir,
        user_id=user_id,
        notes=notes
    )


if __name__ == "__main__":
    main()
