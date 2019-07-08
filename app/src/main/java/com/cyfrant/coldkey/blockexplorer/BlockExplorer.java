package com.cyfrant.coldkey.blockexplorer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BlockExplorer {
    private static final Map<String, BlockExplorer> cache = new ConcurrentHashMap<>();
    private static final String URL_BASE = "https://chain.so";
    private static final int CONF_MIN = 2;
    private final String coin;
    private final ObjectMapper json;

    public BlockExplorer(String coin) {
        this.coin = coin;
        this.json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public BigDecimal balance(String address) throws Exception {
        String method = "/api/v2/get_address_balance/{NETWORK}/{ADDRESS}/{CONFIRMATIONS}"
                .replace("{NETWORK}", coin)
                .replace("{ADDRESS}", address)
                .replace("{CONFIRMATIONS}", Integer.toString(CONF_MIN));
        String response = get(method);
        ApiResponse<Balance> result = json.readValue(response,
                json.getTypeFactory().constructParametricType(ApiResponse.class, Balance.class)
        );
        if (!"success".equalsIgnoreCase(result.getStatus())) {
            throw new IllegalStateException("Balance request for " + coin + " " + address + " failed: " + response);
        }
        return result.getData().getBalance();
    }

    public List<Transactions.Transaction> unspent(String address) throws Exception {
        String method = "/api/v2/get_tx_unspent/{NETWORK}/{ADDRESS}"
                .replace("{NETWORK}", coin)
                .replace("{ADDRESS}", address);
        String response = get(method);
        ApiResponse<Transactions> parsed = json.readValue(response,
                json.getTypeFactory().constructParametricType(ApiResponse.class,
                        Transactions.class
                )
        );
        if (!"success".equalsIgnoreCase(parsed.getStatus())) {
            throw new IllegalStateException("UTXO request for " + coin + " " + address + " failed: " + response);
        }
        List<Transactions.Transaction> result = new CopyOnWriteArrayList<>();
        result.addAll(parsed.getData().getTxs());
        return result;
    }

    public List<Transactions.Transaction> received(String address) throws Exception {
        String method = "/api/v2/get_tx_received/{NETWORK}/{ADDRESS}"
                .replace("{NETWORK}", coin)
                .replace("{ADDRESS}", address);
        String response = get(method);
        ApiResponse<Transactions> parsed = json.readValue(response,
                json.getTypeFactory().constructParametricType(ApiResponse.class,
                        Transactions.class
                )
        );
        if (!"success".equalsIgnoreCase(parsed.getStatus())) {
            throw new IllegalStateException("Received TXs request for " + coin + " " + address + " failed: " + response);
        }
        List<Transactions.Transaction> result = new CopyOnWriteArrayList<>();
        result.addAll(parsed.getData().getTxs());
        return result;
    }

    public List<Transactions.Transaction> spent(String address) throws Exception {
        String method = "/api/v2/get_tx_spent/{NETWORK}/{ADDRESS}"
                .replace("{NETWORK}", coin)
                .replace("{ADDRESS}", address);
        String response = get(method);
        ApiResponse<Transactions> parsed = json.readValue(response,
                json.getTypeFactory().constructParametricType(ApiResponse.class,
                        Transactions.class
                )
        );
        if (!"success".equalsIgnoreCase(parsed.getStatus())) {
            throw new IllegalStateException("Spent TXs request for " + coin + " " + address + " failed: " + response);
        }
        List<Transactions.Transaction> result = new CopyOnWriteArrayList<>();
        result.addAll(parsed.getData().getTxs());
        return result;
    }

    private String get(String method) throws Exception {
        Response response = new OkHttpClient()
                .newCall(
                        new Request.Builder()
                                .url(URL_BASE + method)
                                .get()
                                .build()
                )
                .execute();
        if (response == null) {
            throw new IllegalStateException("Response is null");
        }

        if (response.code() != 200) {
            throw new IllegalStateException(method + " returned " + response.code());
        }

        String result = response.body().string();
        return result;
    }

    public static BlockExplorer forCurrency(final String symbol) {
        String key = symbol.toUpperCase();
        synchronized (cache) {
            if (cache.get(key) == null) {
                cache.put(key, new BlockExplorer(key));
            }
        }
        return cache.get(key);
    }
}
