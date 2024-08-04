curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiMDNiODIxMDA2MjkyNWI2ZWNlNzYyYTU5NmU1MGIzOGIiLCJleHAiOjE3MjI3NzkyOTkxMjQsInRpbWVzdGFtcCI6MTcyMjc3NzQ5OTEzOH0.P7kU26Z1AO0HKvQFO_EVVIbLd1dw6IVa81EEtmp68Uc" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions
