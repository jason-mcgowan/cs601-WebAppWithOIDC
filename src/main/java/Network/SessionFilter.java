package Network;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Checks incoming requests for the session cookie and id. If user's session data does not exist, it
 * is created. The session data (new or existing) is attached as an attribute to the exchange.
 *
 * @author Jason McGowan
 */
public class SessionFilter extends Filter {

  public static final String COOKIE_MAP_ATT = "cookies";
  public static final String SESSION_DATA_ATT = "sessionData";
  public static final String SESSION_ID_COOKIE_KEY = "session";

  private final ConcurrentMap<String, SessionData> sessions;

  public SessionFilter(ConcurrentMap<String, SessionData> sessions) {
    this.sessions = sessions;
  }

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    Map<String, String> cookies = extractCookies(exchange);
    exchange.setAttribute(COOKIE_MAP_ATT, cookies);

    SessionData sessionData = pullOrCreateSessionData(cookies, exchange);
    sessionData.setTimeLastActive(Instant.now());
    exchange.setAttribute(SESSION_DATA_ATT, sessionData);
    chain.doFilter(exchange);
    if (sessionData.isLogoutRequested()) {
      sessionData.setLoggedIn(false);
      sessions.remove(sessionData.getId());
    }
  }

  @Override
  public String description() {
    return "Adds two attributes to the exchange\r\n"
        + SESSION_DATA_ATT + ": A SessionData object\r\n"
        + COOKIE_MAP_ATT + ": A Map<String, String> of cookies";
  }

  private SessionData pullOrCreateSessionData(Map<String, String> cookies, HttpExchange exchange) {
    String sessionId = cookies.get(SESSION_ID_COOKIE_KEY);
    if (sessionId == null) {
      return createNewSessionData(exchange);
    }
    SessionData sessionData = sessions.get(sessionId);
    if (sessionData == null) {
      exchange.getResponseHeaders()
          .add("Set-Cookie", SESSION_ID_COOKIE_KEY + "=; Max-Age=0; Path=/");
      return createNewSessionData(exchange);
    }
    return sessionData;
  }

  private SessionData createNewSessionData(HttpExchange exchange) {
    SessionData sessionData = new SessionData();
    sessions.put(sessionData.getId(), sessionData);
    exchange.getResponseHeaders()
        .add("Set-Cookie",
            SESSION_ID_COOKIE_KEY + "=" + sessionData.getId() + "; Max-Age=3600; Path=/");
    return sessionData;
  }

  private Map<String, String> extractCookies(HttpExchange exchange) {
    Map<String, String> cookies = new HashMap<>();
    List<String> cookieList = exchange.getRequestHeaders().get("Cookie");
    if (cookieList == null) {
      return cookies;
    }

    for (String cookie : cookieList) {
      int i = cookie.indexOf("=");
      String key = cookie.substring(0, i);
      String value = cookie.substring(i + 1);
      cookies.put(key, value);
    }
    return cookies;
  }

}
