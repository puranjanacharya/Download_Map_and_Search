package com.here.tcsdemo;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * GeoNetworkRequest - Class to make network request
 */
public class GeoNetworkRequest {

    private HttpURLConnection mHttpURLConnection;

    private String mServerRes;

    /**
     * Get suggestion from server.
     *
     * @param url URL to hit Server.
     * @return List of location
     */
    public List<String> getResultList(String url) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        DataInputStream dataInputStream = null;
        try {
            InputStream inputStream = null;
            URL urlToConnect = new URL(url);
            mHttpURLConnection = (HttpURLConnection) urlToConnect.openConnection();
            mHttpURLConnection.setRequestProperty("user-agent", "Android");
            mHttpURLConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            mHttpURLConnection.setRequestProperty("Accept", "*/*");
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection.setConnectTimeout(500);
            mHttpURLConnection.connect();
            int responseCode = mHttpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = mHttpURLConnection.getInputStream();
                dataInputStream = new DataInputStream(inputStream);
                int a = -1;
                byte b[] = new byte[1024];
                byteArrayOutputStream = new ByteArrayOutputStream();
                while ((a = dataInputStream.read(b)) != -1) {
                    byteArrayOutputStream.write(b, 0, a);
                }
                mServerRes = new String(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.close();
                dataInputStream.close();
                mHttpURLConnection.disconnect();
            } else {
                mHttpURLConnection.disconnect();
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                }
                byteArrayOutputStream = null;
            }
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                }
                dataInputStream = null;
            }
            if (mHttpURLConnection != null) {
                mHttpURLConnection.disconnect();
                mHttpURLConnection = null;
            }
        }
        return parseResponse();
    }

    /**
     * Parse the server response
     *
     * @return List<String> parsed response
     */
    private List<String> parseResponse() {
        if (mServerRes != null && mServerRes.length() != 0) {
            try {
                final ResponseModel parseResponse = new Gson().fromJson(mServerRes,
                        ResponseModel.class);
                List<String> responseList = new ArrayList<>();
                List<ResponseModel.Suggestion> suggestionList = parseResponse.getSuggestions();
                if (suggestionList != null && suggestionList.size() > 0) {
                    for (ResponseModel.Suggestion suggestion : suggestionList) {
                        responseList.add(suggestion.getLabel());
                    }
                }
                return responseList;
            } catch (Exception e) {
                // no need to do anything
            }
        }
        return null;
    }
}
