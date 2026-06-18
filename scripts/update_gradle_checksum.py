#!/usr/bin/env python3
import os
import re
import urllib.request
import hashlib
import sys

PROPERTIES_PATH = 'gradle/wrapper/gradle-wrapper.properties'

def get_gradle_version_and_url():
    if not os.path.exists(PROPERTIES_PATH):
        print(f"Error: {PROPERTIES_PATH} not found.")
        sys.exit(1)
    
    with open(PROPERTIES_PATH, 'r') as f:
        content = f.read()
    
    match = re.search(r'distributionUrl=(.+)', content)
    if not match:
        print("Error: Could not find distributionUrl in properties file.")
        sys.exit(1)
    
    url = match.group(1).replace('\\:', ':')
    # Extract version from URL like .../gradle-8.5-bin.zip
    version_match = re.search(r'gradle-([\d\.]+)-(bin|all)\.zip', url)
    if not version_match:
        print(f"Error: Could not parse version from URL: {url}")
        sys.exit(1)
    
    version = version_match.group(1)
    dist_type = version_match.group(2)
    return version, url, dist_type

def fetch_sha256(version, dist_type):
    # Gradle provides SHA-256 at services.gradle.org/distributions/gradle-X.Y.Z-bin.zip.sha256
    sha_url = f"https://services.gradle.org/distributions/gradle-{version}-{dist_type}.zip.sha256"
    print(f"Fetching SHA-256 from: {sha_url}")
    try:
        with urllib.request.urlopen(sha_url) as response:
            return response.read().decode('utf-8').strip()
    except Exception as e:
        print(f"Error fetching SHA-256: {e}")
        sys.exit(1)

def update_properties(sha256):
    with open(PROPERTIES_PATH, 'r') as f:
        lines = f.readlines()
    
    updated = False
    new_lines = []
    sha_line = f"distributionSha256Sum={sha256}\n"
    
    for line in lines:
        if line.startswith('distributionSha256Sum='):
            new_lines.append(sha_line)
            updated = True
        else:
            new_lines.append(line)
            
    if not updated:
        # If not found, insert it after distributionUrl
        final_lines = []
        for line in new_lines:
            final_lines.append(line)
            if line.startswith('distributionUrl='):
                final_lines.append(sha_line)
                updated = True
        new_lines = final_lines

    with open(PROPERTIES_PATH, 'w') as f:
        f.writelines(new_lines)
    
    print(f"Successfully updated {PROPERTIES_PATH} with SHA-256: {sha256}")

if __name__ == "__main__":
    version, url, dist_type = get_gradle_version_and_url()
    print(f"Detected Gradle version: {version} ({dist_type})")
    sha256 = fetch_sha256(version, dist_type)
    update_properties(sha256)
