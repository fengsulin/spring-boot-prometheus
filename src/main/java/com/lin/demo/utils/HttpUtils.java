package com.lin.demo.utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    private static final int CONNECT_TIMEOUT = Config.getHttpConnectTimeout();// ????????????????????????????????????10s
    private static final int SOCKET_TIMEOUT = Config.getHttpSocketTimeout();
    private static final int MAX_CONN = Config.getHttpMaxPoolSize(); // ???????????????
    private static final int Max_PRE_ROUTE = Config.getHttpMaxPoolSize();
    private static final int MAX_ROUTE = Config.getHttpMaxPoolSize();
    private static CloseableHttpClient httpClient; // ??????????????????????????????
    private static PoolingHttpClientConnectionManager manager; //??????????????????
    private static ScheduledExecutorService monitorExecutor;

    private final static Object syncLock = new Object(); // ??????????????????,??????????????????

    /**
     * ???http????????????????????????
     * @param httpRequestBase http??????
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase){
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();

        httpRequestBase.setConfig(requestConfig);
    }

    public static CloseableHttpClient getHttpClient(String url){
        String hostName = url.split("/")[2];
        System.out.println(hostName);
        int port = 80;
        if (hostName.contains(":")){
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }

        if (httpClient == null){
            //????????????????????????????????????getHttpClient????????????????????????httpClient???????????????,????????????????????????
            synchronized (syncLock){
                if (httpClient == null){
                    httpClient = createHttpClient(hostName, port);
                    //??????????????????,????????????????????????????????????
                    monitorExecutor = Executors.newScheduledThreadPool(1);
                    monitorExecutor.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            //??????????????????
                            manager.closeExpiredConnections();
                            //??????5s???????????????
                            manager.closeIdleConnections(Config.getHttpIdelTimeout(), TimeUnit.MILLISECONDS);
                            logger.info("close expired and idle for over 5s connection");
                        }
                    }, Config.getHttpMonitorInterval(), Config.getHttpMonitorInterval(), TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * ??????host???port??????httpclient??????
     * @param host ??????????????????
     * @param port ??????????????????
     * @return
     */
    public static CloseableHttpClient createHttpClient(String host, int port){
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        manager = new PoolingHttpClientConnectionManager(registry);
        //??????????????????
        manager.setMaxTotal(MAX_CONN); // ???????????????
        manager.setDefaultMaxPerRoute(Max_PRE_ROUTE); // ?????????????????????

        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);

        //???????????????,??????????????????
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i > 3){
                    //????????????3???,????????????
                    logger.error("retry has more than 3 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException){
                    //?????????????????????,?????????????????????????????????,????????????
                    logger.error("receive no response from server, retry");
                    return true;
                }
                if (e instanceof SSLHandshakeException){
                    // SSL????????????
                    logger.error("SSL hand shake exception");
                    return false;
                }
                if (e instanceof InterruptedIOException){
                    //??????
                    logger.error("InterruptedIOException");
                    return false;
                }
                if (e instanceof UnknownHostException){
                    // ??????????????????
                    logger.error("server host unknown");
                    return false;
                }
                if (e instanceof ConnectTimeoutException){
                    // ????????????
                    logger.error("Connection Time out");
                    return false;
                }
                if (e instanceof SSLException){
                    logger.error("SSLException");
                    return false;
                }

                HttpClientContext context = HttpClientContext.adapt(httpContext);
                HttpRequest request = context.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)){
                    //???????????????????????????????????????
                    return true;
                }
                return false;
            }
        };

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).setRetryHandler(handler).build();
        return client;
    }

    /**
     * ??????post???????????????
     * @param httpPost
     * @param params
     */
    private static void setPostParams(HttpPost httpPost, Map<String, String> params){
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        Set<String> keys = params.keySet();
        for (String key: keys){
            pairs.add(new BasicNameValuePair(key, params.get(key)));
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????get????????????
     * @param httpGet
     * @param url
     * @param params
     */
    private static void setGetParams(HttpGet httpGet,String url,Map<String,Object> params){
        List<NameValuePair> pairs = new ArrayList<>();
        if(params != null && params.size()>0){
            for(Map.Entry<String,Object> entry:params.entrySet()){
                pairs.add(new BasicNameValuePair(entry.getKey(),(String) entry.getValue()));
            }
        }
        try {
            // ???????????????????????????
            URIBuilder uriBuilder = new URIBuilder(url);
            uriBuilder.setParameters(pairs);
            httpGet.setURI(uriBuilder.build());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????post??????
     * @param url
     * @param params
     * @return
     */
    public static JSONObject post(String url, Map<String, String> params){
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        setPostParams(httpPost, params);
        CloseableHttpResponse response = null;
        InputStream in = null;
        JSONObject object = null;
        try {
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                String result = IOUtils.toString(in, "utf-8");
                object = JSONObject.parseObject(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if (in != null) in.close();
                if (response != null) response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * ??????get??????
     * @param url
     * @param params
     * @return
     */
    public static JSONObject get(String url,Map<String,Object> params){
        HttpGet httpGet = new HttpGet();
        setRequestConfig(httpGet);
        setGetParams(httpGet,url,params);
        CloseableHttpResponse response = null;
        InputStream in = null;
        JSONObject object = null;
        try{
            response = getHttpClient(url).execute(httpGet,HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if(entity != null){
                in = entity.getContent();
                String result = IOUtils.toString(in, "utf-8");
                object = JSONObject.parseObject(result);
            }
        }catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in != null) {
                try {
                    in.close();
                    if(response != null) response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    /**
     * ???????????????
     */
    public static void closeConnectionPool(){
        try {
            httpClient.close();
            manager.close();
            monitorExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String url = "http://192.168.2.131:3000/login";
        CloseableHttpClient httpClient = getHttpClient(url);
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        httpPost.setHeader("Content-Type","application/json");

        Map<String,String> object = new HashMap<>();
        object.put("user","admin");
        object.put("password","admin8888");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String s2 = objectMapper.writeValueAsString(object);

            httpPost.setEntity(new StringEntity(s2));
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            Header[] sessions = response.getHeaders("Set-Cookie");
            for (Header session : sessions) {
                String grafana_session = Arrays.stream(session.getElements()).filter(s -> s.getName().equals("grafana_session")).findFirst().get().getValue();

                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~"+grafana_session);
            }
//            System.out.println("grafana?????????sessionId??????"+);
            InputStream content = entity.getContent();
            String s = IOUtils.toString(content, "utf-8");
            String s1 = JSONObject.toJSONString(s);
            System.out.println(s1);
            content.close();
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

