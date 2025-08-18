from fastapi import FastAPI, HTTPException, UploadFile, File, Query
from fastapi.responses import StreamingResponse, JSONResponse
import os
import secrets
import time
from typing import Optional
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="VPN Speed Test Backend",
    description="FastAPI backend for measuring VPN connection speeds",
    version="1.0.0"
)

# Configuration
DEFAULT_DOWNLOAD_SIZE = 100 * 1024 * 1024  # 100MB in bytes
DEFAULT_UPLOAD_SIZE = 10 * 1024 * 1024     # 10MB in bytes
CHUNK_SIZE = 64 * 1024                      # 64KB chunks for streaming
TEMP_DIR = "/tmp/speedtest"

# Ensure temp directory exists
os.makedirs(TEMP_DIR, exist_ok=True)

@app.get("/")
async def root():
    """Root endpoint - server info"""
    return {
        "message": "VPN Speed Test Backend",
        "endpoints": {
            "download": "/download",
            "upload": "/upload", 
            "ping": "/ping"
        },
        "status": "running"
    }

@app.get("/ping")
async def ping():
    """
    Ping endpoint to test basic connectivity and measure latency.
    
    Android Integration:
    - Use this endpoint to measure ping/latency to the server
    - Measure time between request and response
    - Useful for determining which server is closest geographically
    """
    return {"message": "pong", "timestamp": time.time()}

@app.get("/download")
async def download_file(
    size: Optional[int] = Query(
        DEFAULT_DOWNLOAD_SIZE, 
        description="File size in bytes (default: 100MB)",
        ge=1024,  # Minimum 1KB
        le=1024*1024*1024  # Maximum 1GB
    )
):
    """
    Download endpoint that streams a large file for speed testing.
    
    Android Integration:
    - Use this endpoint to measure download speed
    - Stream the response and measure bytes received per second
    - Adjust 'size' parameter to test different file sizes
    - Monitor network speed while connected through VPN
    
    Example Android usage:
    ```kotlin
    val response = client.get("/download?size=52428800") // 50MB
    val inputStream = response.body?.byteStream()
    // Measure download speed by timing bytes received
    ```
    """
    try:
        logger.info(f"Starting download request for {size} bytes")
        
        def generate_file():
            """Generate random data in chunks to avoid memory issues"""
            remaining = size
            while remaining > 0:
                chunk_size = min(CHUNK_SIZE, remaining)
                # Generate random data chunk
                chunk = secrets.token_bytes(chunk_size)
                remaining -= chunk_size
                yield chunk
        
        # Return streaming response with appropriate headers
        return StreamingResponse(
            generate_file(),
            media_type="application/octet-stream",
            headers={
                "Content-Disposition": f"attachment; filename=speedtest_{size}.bin",
                "Content-Length": str(size),
                "Cache-Control": "no-cache, no-store, must-revalidate"
            }
        )
        
    except Exception as e:
        logger.error(f"Download error: {str(e)}")
        raise HTTPException(status_code=500, detail="Download failed")

@app.post("/upload")
async def upload_file(
    file: UploadFile = File(...),
    expected_size: Optional[int] = Query(
        DEFAULT_UPLOAD_SIZE,
        description="Expected file size in bytes (default: 10MB)",
        ge=1024,  # Minimum 1KB
        le=100*1024*1024  # Maximum 100MB
    )
):
    """
    Upload endpoint that accepts files for speed testing.
    
    Android Integration:
    - Use this endpoint to measure upload speed
    - Send a file and measure time taken
    - Calculate upload speed in bytes per second
    - Test with different file sizes by adjusting 'expected_size'
    
    Example Android usage:
    ```kotlin
    val file = File.createTempFile("speedtest", ".bin")
    // Fill file with random data up to expected_size
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", file.name, file.asRequestBody())
        .build()
    
    val response = client.post("/upload?expected_size=5242880") // 5MB
    ```
    """
    try:
        logger.info(f"Starting upload request for file: {file.filename}")
        
        # Validate file size
        if file.size and file.size > expected_size:
            raise HTTPException(
                status_code=400, 
                detail=f"File size {file.size} exceeds expected size {expected_size}"
            )
        
        # Create temporary file path
        temp_file_path = os.path.join(TEMP_DIR, f"upload_{int(time.time())}_{file.filename}")
        
        # Stream file to disk in chunks to avoid memory issues
        total_bytes = 0
        start_time = time.time()
        
        with open(temp_file_path, "wb") as buffer:
            while chunk := await file.read(CHUNK_SIZE):
                buffer.write(chunk)
                total_bytes += len(chunk)
                
                # Check if we're exceeding expected size
                if total_bytes > expected_size:
                    # Clean up and return error
                    buffer.close()
                    os.unlink(temp_file_path)
                    raise HTTPException(
                        status_code=400,
                        detail=f"File size {total_bytes} exceeds expected size {expected_size}"
                    )
        
        upload_time = time.time() - start_time
        upload_speed = total_bytes / upload_time if upload_time > 0 else 0
        
        # Clean up temporary file
        os.unlink(temp_file_path)
        
        logger.info(f"Upload completed: {total_bytes} bytes in {upload_time:.2f}s")
        
        return JSONResponse({
            "message": "Upload successful",
            "filename": file.filename,
            "size_bytes": total_bytes,
            "upload_time_seconds": round(upload_time, 3),
            "upload_speed_bps": round(upload_speed, 2),
            "upload_speed_mbps": round(upload_speed / (1024 * 1024), 2)
        })
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Upload error: {str(e)}")
        raise HTTPException(status_code=500, detail="Upload failed")

@app.get("/health")
async def health_check():
    """Health check endpoint for monitoring"""
    return {"status": "healthy", "timestamp": time.time()}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
