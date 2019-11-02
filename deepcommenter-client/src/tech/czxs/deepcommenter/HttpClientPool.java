package tech.czxs.deepcommenter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


    public class HttpClientPool {

        private static volatile HttpClientPool clientInstance;
        private static PoolingHttpClientConnectionManager connectionManager;


        /**
         * create a client pool for http client .
         */
        /**
         * create a client pool for http client .
         */
        /**
         * create a client pool for http client .
         */
        public static HttpClientPool getHttpClient() {
            HttpClientPool tmp = clientInstance;
            if (tmp == null) {
                synchronized (HttpClientPool.class) {
                    tmp = clientInstance;
                    if (tmp == null) {
                        tmp = new HttpClientPool();
                        clientInstance = tmp;
                    }
                }
            }
            return tmp;
        }

    /**
     * creates a new httpclient object .
     */
    /**
     * creates a new httpclient object .
     */
    private HttpClientPool() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);// 连接池
        connectionManager.setDefaultMaxPerRoute(100);// 每条通道的并发连接数
    }

    private CloseableHttpClient getHttpClient(int connectionTimeout, int socketTimeOut) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionTimeout).setSocketTimeout(socketTimeOut).build();
        return HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build();
    }

    public String get(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        return getResponseContent(url,httpGet);
    }

    public String post(String url, String code) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("s", code));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        return getResponseContent(url, httpPost);
    }

    private String getResponseContent(String url, HttpRequestBase request) throws Exception {
        HttpResponse response = null;
        try {
            response = this.getHttpClient(15000,15000).execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            throw new Exception("got an error from HTTP for url : " + URLDecoder.decode(url, "UTF-8"),e);
        } finally {
            if(response != null){
                EntityUtils.consumeQuietly(response.getEntity());
            }
            request.releaseConnection();
        }
    }
}