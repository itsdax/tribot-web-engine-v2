package scripts.dax.walker.server;

import com.allatori.annotations.DoNotRename;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import okhttp3.*;
import scripts.dax.common.DaxLogger;
import scripts.dax.walker.data.BulkBankPathRequest;
import scripts.dax.walker.data.BulkPathRequest;
import scripts.dax.walker.data.PathResult;
import scripts.dax.walker.data.exceptions.AuthorizationException;
import scripts.dax.walker.data.exceptions.RateLimitException;
import scripts.dax.walker.data.exceptions.UnknownException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DoNotRename
public class DaxWalkerServerClient {

    private static final String BASE_URL = "https://api.dax.cloud/walker";

    private final Gson gson;
    private final String key;
    private final String secret;
    private final OkHttpClient okHttpClient;

    private long rateLimit;

    public DaxWalkerServerClient(String key, String secret) {
        this.gson = new Gson();
        this.key = key;
        this.secret = secret;
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .callTimeout(8, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(0, 10L, TimeUnit.MINUTES))
                .protocols(List.of(Protocol.HTTP_1_1))
                .build();
        this.rateLimit = 0L;
    }

    public List<PathResult> getPaths(BulkPathRequest bulkPathRequest) {
        return makePathRequest(BASE_URL + "/generatePaths", gson.toJson(bulkPathRequest));
    }

    public List<PathResult> getBankPaths(BulkBankPathRequest bulkBankPathRequest) {
        return makePathRequest(BASE_URL + "/generateBankPaths", gson.toJson(bulkBankPathRequest));
    }

    private List<PathResult> makePathRequest(String url, String jsonPayload) {
        if (System.currentTimeMillis() - rateLimit < 5000L)
            throw new RateLimitException("Throttling requests because  of recent key rate limit.");

        DaxLogger.info("%s", jsonPayload);

        Request request = generateRequest(url, RequestBody.create(MediaType.parse("application/json"), jsonPayload));

        try {
            long start = System.currentTimeMillis();
            Response response = okHttpClient.newCall(request).execute();
            DaxLogger.info("Call took %dms", System.currentTimeMillis() - start);
            switch (response.code()) {
                case 429 -> {
                    DaxLogger.error("Api key rate limit exceeded. Response: %s", response.message());
                    this.rateLimit = System.currentTimeMillis();
                    throw new RateLimitException(response.message());
                }
                case 401 -> {
                    DaxLogger.error("Api is unauthorized. Response: %s", response.message());
                    throw new AuthorizationException(String.format("Invalid API Key [%s]", response.message()));
                }
                case 200 -> {
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            DaxLogger.error("Call succeeded but contents are empty");
                            throw new IllegalStateException("Illegal response returned from server.");
                        }
                        return gson.fromJson(responseBody.string(), new TypeToken<List<PathResult>>() {
                        }.getType());
                    }
                }
                default -> {
                    DaxLogger.error("Unknown response code returned from server");
                    throw new IllegalStateException("Unknown response code returned from server.");
                }
            }
        } catch (IOException e) {
            DaxLogger.info("Error making call: %s", e.toString());
            throw new UnknownException("Error connecting to server.");
        }
    }

    private Request generateRequest(String url, RequestBody body) {
        return new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("key", key)
                .addHeader("secret", secret)
//                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Tribot-EngineV2")
                .build();
    }

}