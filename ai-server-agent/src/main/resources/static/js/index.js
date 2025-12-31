import axios from "axios";

const API_BASE_URL =
  url
  // 'http://172.29.116.27:8503'
   + ""; // 将被代理到 http://localhost/v1
const API_TOKEN = "0e7892d7c95543cfabeddc69311cd983";

console.log(url)

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
    "Authorization": `${API_TOKEN}`,
  },
});


function sendStreamMessage2(query, handlers) {
	const { onMessage, onThought, onFile, onComplete, onError } = handlers;
	// var data = new FormData();
	console.log(handlers,'0-1')
	const params = {
		requestAi: {
			message: query,
			userId: '',
			queryType: '',
			media: '',	 
			mimeType: '',
		},
		file: null
	};
	console.log(params.requestAi)
	// data.append('requestAi', JSON.stringify(params.requestAi) /* , {contentType: 'application/json'} */);
	// data.append('file', '');
	
	var formData = new FormData();
	// formdata.append("requestAi", "{\n    \"message\": \"查询2025年9月30号杭州的停电用户数\",\n    \"userId\": \"\",\n    \"queryType\": \"\",\n    \"media\": \"\",\n    \"mimeType\": \"\"\n}");
	
	formData.append('requestAi', new Blob([JSON.stringify(params.requestAi)], {
	    type: 'application/json'
	}));
	
	formData.append("file", null );

console.log(formData)

  fetch(`${API_BASE_URL}/c2000/ai/chat`, {
    method: "POST",
    headers: {
        'Accept': 'text/event-stream',
        'Cache-Control': 'no-cache',
		"Authorization": window.__MICRO_APP_ENVIRONMENT__
      ? window.microApp.getData()?.token || `${API_TOKEN}`
      : sessionStorage.getItem('token') || `${API_TOKEN}`,
    },
    body: formData,
  })
    .then((response) => {
      console.log("=-=-==========", response);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      if (!response.body) {
        throw new Error("Response body is null");
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();

      let buffer = "";

      function read() {
        reader
          .read()
          .then(({ done, value }) => {
			  console.log(done,value,'qqqqqqqqqqqqqqqqqqqqqqqq')
            if (done) {
              console.log("流式响应完成");
              onComplete && onComplete();
              return;
            }

            const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;

            console.log("----------->chunk", chunk);

            // 处理SSE格式数据
            const lines = buffer.split("\n");
            buffer = lines.pop() || "";
console.log("----------->buffer", buffer);
            for (const line of lines) {
              if (line.trim().startsWith("data:")) {
                try {
                  const jsonStr = line.substring(5).trim();
                  if (!jsonStr) continue;

                  console.log("接收到数据片段:", jsonStr);
                  const eventData = JSON.parse(jsonStr);
				console.log("eventData:", eventData);
                  // 根据事件类型处理数据
				  // return false;
                  switch (eventData.event) {
                    case "message":
                    case "agent_log":
                     // 处理消息文本
                     if (eventData.data !== undefined) {
                      console.log(eventData.data,'1122')
                                onMessage && onMessage(eventData);
                              }
                              break;
                    case "agent_table" || "agent_table_limit":
                      // 处理消息文本
                      if (eventData.data !== undefined) {
                        console.log(eventData.data,'1122')
                                  onMessage && onMessage(eventData);
                                }
                                break;
                    case "agent_table_limit":
                      // 处理消息文本
                      if (eventData.data !== undefined) {
                        console.log(eventData.data,'1122')
                                  onMessage && onMessage(eventData);
                                }
                                break;
                    case "agent_message":
                      // 处理消息文本
                      if (eventData.answer !== undefined) {
						  console.log(eventData.answer)
                        onMessage && onMessage(eventData.answer);
                      }
                      break;

                    case "agent_thought":
                      // 处理 agent 思考过程
                      onThought && onThought(eventData);
                      break;

                    case "message_file":
                      // 处理文件消息（如图片）
                      onFile && onFile(eventData);
                      break;

                    case "message_end":
                    case "tts_message_end":
                      // 消息结束事件
                      console.log("接收到消息结束事件");
                      break;

                    case "error":
                      // 错误事件
                      console.error("流中的错误:", eventData);
                      onError && onError(eventData.error || "流处理错误");
                      break;

                    default:
                      console.log("未处理的事件类型:", eventData.event);
                  }
                } catch (e) {
                  console.error("JSON解析错误:", e);
                }
              }
            }

            read();
          })
          .catch((error) => {
            console.error("读取流出错:", error);
            onError && onError(error.message);
          });
      }

      read();
    })
    .catch((error) => {
      console.error("请求出错:", error);
      onError && onError(error.message);
    });
}

// Export the API functions as an object
export const chatApi = {
  sendStreamMessage2,
};

// Also export the functions individually for backward compatibility
export { sendStreamMessage2 };
