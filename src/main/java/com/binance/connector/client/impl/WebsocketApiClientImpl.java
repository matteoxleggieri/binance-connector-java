package com.binance.connector.client.impl;

import com.binance.connector.client.WebsocketApiClient;
import com.binance.connector.client.enums.DefaultUrls;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.impl.websocketapi.WebSocketApiAccount;
import com.binance.connector.client.impl.websocketapi.WebSocketApiGeneral;
import com.binance.connector.client.impl.websocketapi.WebSocketApiMarket;
import com.binance.connector.client.impl.websocketapi.WebSocketApiTrade;
import com.binance.connector.client.impl.websocketapi.WebSocketApiUserDataStream;
import com.binance.connector.client.utils.RequestBuilder;
import com.binance.connector.client.utils.SignatureGenerator;
import com.binance.connector.client.utils.WebSocketCallback;
import com.binance.connector.client.utils.WebSocketConnection;
import com.binance.connector.client.utils.httpclient.WebSocketApiHttpClientSingleton;
import com.binance.connector.client.utils.websocketapi.WebSocketApiRequestHandler;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class WebsocketApiClientImpl implements WebsocketApiClient {
    private static final OkHttpClient client = WebSocketApiHttpClientSingleton.getHttpClient();
    private final SignatureGenerator signatureGenerator;
    private final String apiKey;
    private final String baseUrl;
    private final WebSocketCallback noopCallback = msg -> { };
    private WebSocketConnection connection; 
    private WebSocketApiRequestHandler requestHandler;
    private WebSocketApiGeneral wsApiGeneral;
    private WebSocketApiMarket wsApiMarket;
    private WebSocketApiTrade wsApiTrade;
    private WebSocketApiAccount wsApiAccount;
    private WebSocketApiUserDataStream wsApiUserDataStream;

    public WebsocketApiClientImpl() {
        this("", null);
    }

    public WebsocketApiClientImpl(String baseUrl) {
        this("", null, baseUrl);
    }

    public WebsocketApiClientImpl(String apiKey, SignatureGenerator signatureGenerator) {
        this(apiKey, signatureGenerator, DefaultUrls.WS_API_URL);
    }

    public WebsocketApiClientImpl(String apiKey, SignatureGenerator signatureGenerator, String baseUrl) {
        this.apiKey = apiKey;
        this.signatureGenerator = signatureGenerator;
        this.baseUrl = baseUrl;
    }
    
    private void checkRequestHandler() {
        if (this.requestHandler == null) {
            throw new BinanceConnectorException("No Websocket API connection to submit request. Please connect first.");
        }
    }

    private void checkCategoryInstance(Object categoryInstance, Class<?> categoryClass) {
        if (categoryInstance == null) {
            if (categoryClass == WebSocketApiGeneral.class) {
                this.wsApiGeneral = new WebSocketApiGeneral(this.requestHandler);
            } else if (categoryClass == WebSocketApiMarket.class) {
                this.wsApiMarket = new WebSocketApiMarket(this.requestHandler);
            } else if (categoryClass == WebSocketApiTrade.class) {
                this.wsApiTrade = new WebSocketApiTrade(this.requestHandler);
            } else if (categoryClass == WebSocketApiAccount.class) {
                this.wsApiAccount = new WebSocketApiAccount(this.requestHandler);
            } else if (categoryClass == WebSocketApiUserDataStream.class) {
                this.wsApiUserDataStream = new WebSocketApiUserDataStream(this.requestHandler);
            }
        }
    }

    @Override
    public void connect(WebSocketCallback onMessageCallback) {
        connect(noopCallback, onMessageCallback, noopCallback, noopCallback);
    }

    public void connect(WebSocketCallback onOpenCallback, WebSocketCallback onMessageCallback, WebSocketCallback onClosingCallback, WebSocketCallback onFailureCallback) {
        Request request = RequestBuilder.buildWebsocketRequest(baseUrl);

        this.connection = new WebSocketConnection(onOpenCallback, onMessageCallback, onClosingCallback, onFailureCallback, request, client);
        this.requestHandler = new WebSocketApiRequestHandler(this.connection, this.apiKey, this.signatureGenerator);
        this.connection.connect();
    }

    @Override
    public void close() {
        this.connection.close();
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public WebSocketApiGeneral general() {
        checkRequestHandler();
        checkCategoryInstance(this.wsApiGeneral, WebSocketApiGeneral.class);
        return this.wsApiGeneral;
    }

    @Override
    public WebSocketApiMarket market() {
        checkRequestHandler();
        checkCategoryInstance(this.wsApiMarket, WebSocketApiMarket.class);
        return this.wsApiMarket;
    }

    @Override
    public WebSocketApiTrade trade() {
        checkRequestHandler();
        checkCategoryInstance(this.wsApiTrade, WebSocketApiTrade.class);
        return this.wsApiTrade;
    }

    @Override
    public WebSocketApiAccount account() {
        checkRequestHandler();
        checkCategoryInstance(this.wsApiAccount, WebSocketApiAccount.class);
        return this.wsApiAccount;
    }

    @Override
    public WebSocketApiUserDataStream userDataStream() {
        checkRequestHandler();
        checkCategoryInstance(this.wsApiUserDataStream, WebSocketApiUserDataStream.class);
        return this.wsApiUserDataStream;
    }

}