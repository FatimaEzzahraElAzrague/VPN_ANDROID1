#!/usr/bin/env python3
"""
Test script for VPN Speed Test Backend endpoints
Run this script to verify all endpoints are working correctly
"""

import requests
import time
import tempfile
import os
from typing import List

class SpeedTestClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip('/')
        self.session = requests.Session()
        
    def test_ping(self) -> dict:
        """Test ping endpoint and measure latency"""
        print(f"🔍 Testing ping endpoint: {self.base_url}/ping")
        
        start_time = time.time()
        try:
            response = self.session.get(f"{self.base_url}/ping", timeout=10)
            end_time = time.time()
            
            if response.status_code == 200:
                latency_ms = (end_time - start_time) * 1000
                result = response.json()
                print(f"✅ Ping successful - Latency: {latency_ms:.2f}ms")
                print(f"   Response: {result}")
                return {"success": True, "latency_ms": latency_ms, "response": result}
            else:
                print(f"❌ Ping failed - Status: {response.status_code}")
                return {"success": False, "status_code": response.status_code}
                
        except Exception as e:
            print(f"❌ Ping error: {str(e)}")
            return {"success": False, "error": str(e)}
    
    def test_download(self, size_mb: int = 10) -> dict:
        """Test download endpoint with specified file size"""
        size_bytes = size_mb * 1024 * 1024
        print(f"🔍 Testing download endpoint: {self.base_url}/download?size={size_bytes}")
        
        try:
            start_time = time.time()
            response = self.session.get(
                f"{self.base_url}/download?size={size_bytes}", 
                stream=True,
                timeout=30
            )
            
            if response.status_code == 200:
                total_bytes = 0
                chunk_size = 8192
                
                # Stream the response and count bytes
                for chunk in response.iter_content(chunk_size=chunk_size):
                    total_bytes += len(chunk)
                
                end_time = time.time()
                duration = end_time - start_time
                speed_mbps = (total_bytes / duration) / (1024 * 1024)
                
                print(f"✅ Download successful - {size_mb}MB in {duration:.2f}s")
                print(f"   Speed: {speed_mbps:.2f} Mbps")
                print(f"   Bytes received: {total_bytes:,}")
                
                return {
                    "success": True,
                    "size_mb": size_mb,
                    "duration_s": duration,
                    "speed_mbps": speed_mbps,
                    "bytes_received": total_bytes
                }
            else:
                print(f"❌ Download failed - Status: {response.status_code}")
                return {"success": False, "status_code": response.status_code}
                
        except Exception as e:
            print(f"❌ Download error: {str(e)}")
            return {"success": False, "error": str(e)}
    
    def test_upload(self, size_mb: int = 5) -> dict:
        """Test upload endpoint with specified file size"""
        size_bytes = size_mb * 1024 * 1024
        print(f"🔍 Testing upload endpoint: {self.base_url}/upload?expected_size={size_bytes}")
        
        try:
            # Create a temporary test file
            with tempfile.NamedTemporaryFile(delete=False, suffix='.bin') as temp_file:
                # Fill with random data
                temp_file.write(os.urandom(size_bytes))
                temp_file_path = temp_file.name
            
            try:
                # Upload the file
                start_time = time.time()
                
                with open(temp_file_path, 'rb') as f:
                    files = {'file': (f'test_upload_{size_mb}mb.bin', f, 'application/octet-stream')}
                    response = self.session.post(
                        f"{self.base_url}/upload?expected_size={size_bytes}",
                        files=files,
                        timeout=60
                    )
                
                end_time = time.time()
                duration = end_time - start_time
                
                if response.status_code == 200:
                    result = response.json()
                    speed_mbps = (size_bytes / duration) / (1024 * 1024)
                    
                    print(f"✅ Upload successful - {size_mb}MB in {duration:.2f}s")
                    print(f"   Speed: {speed_mbps:.2f} Mbps")
                    print(f"   Response: {result}")
                    
                    return {
                        "success": True,
                        "size_mb": size_mb,
                        "duration_s": duration,
                        "speed_mbps": speed_mbps,
                        "response": result
                    }
                else:
                    print(f"❌ Upload failed - Status: {response.status_code}")
                    print(f"   Response: {response.text}")
                    return {"success": False, "status_code": response.status_code}
                    
            finally:
                # Clean up temporary file
                os.unlink(temp_file_path)
                
        except Exception as e:
            print(f"❌ Upload error: {str(e)}")
            return {"success": False, "error": str(e)}
    
    def test_root(self) -> dict:
        """Test root endpoint"""
        print(f"🔍 Testing root endpoint: {self.base_url}/")
        
        try:
            response = self.session.get(f"{self.base_url}/", timeout=10)
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ Root endpoint successful")
                print(f"   Response: {result}")
                return {"success": True, "response": result}
            else:
                print(f"❌ Root endpoint failed - Status: {response.status_code}")
                return {"success": False, "status_code": response.status_code}
                
        except Exception as e:
            print(f"❌ Root endpoint error: {str(e)}")
            return {"success": False, "error": str(e)}
    
    def run_all_tests(self) -> dict:
        """Run all tests and return summary"""
        print(f"🚀 Starting comprehensive tests for: {self.base_url}")
        print("=" * 60)
        
        results = {
            "server": self.base_url,
            "timestamp": time.time(),
            "tests": {}
        }
        
        # Test root endpoint
        results["tests"]["root"] = self.test_root()
        print()
        
        # Test ping
        results["tests"]["ping"] = self.test_ping()
        print()
        
        # Test download (10MB)
        results["tests"]["download"] = self.test_download(10)
        print()
        
        # Test upload (5MB)
        results["tests"]["upload"] = self.test_upload(5)
        print()
        
        # Summary
        print("=" * 60)
        print("📊 TEST SUMMARY")
        print("=" * 60)
        
        passed = 0
        total = len(results["tests"])
        
        for test_name, result in results["tests"].items():
            status = "✅ PASS" if result.get("success", False) else "❌ FAIL"
            print(f"{test_name.upper():12} : {status}")
            if result.get("success", False):
                passed += 1
        
        print(f"\nResults: {passed}/{total} tests passed")
        
        if passed == total:
            print("🎉 All tests passed! Server is working correctly.")
        else:
            print("⚠️  Some tests failed. Check server configuration.")
        
        return results

def main():
    """Main function to test both servers"""
    servers = [
        "http://56.155.92.31:8000",    # Osaka
        "http://52.47.190.220:8000"    # Paris
    ]
    
    print("🌐 VPN Speed Test Backend - Endpoint Testing")
    print("=" * 60)
    
    all_results = []
    
    for server_url in servers:
        print(f"\n🏗️  Testing server: {server_url}")
        print("-" * 60)
        
        client = SpeedTestClient(server_url)
        results = client.run_all_tests()
        all_results.append(results)
        
        print("\n" + "=" * 60)
    
    # Final summary
    print("\n🎯 FINAL SUMMARY")
    print("=" * 60)
    
    for results in all_results:
        server = results["server"]
        passed = sum(1 for test in results["tests"].values() if test.get("success", False))
        total = len(results["tests"])
        
        status = "✅ READY" if passed == total else "❌ ISSUES"
        print(f"{server:25} : {status} ({passed}/{total})")
    
    print("\n✨ Testing completed!")

if __name__ == "__main__":
    main()
