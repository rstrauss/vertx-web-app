package org.myorg.utils;

import io.vertx.core.http.HttpServerResponse;

/**
 * From:  https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
 *
 * Each item contains 3 send methods to send back a status code and message.
 */
public enum HtmlERR {
    Continue(100),
    SwitchingProtocols(101), // if the client requested, and the server has agreed to switch
    Processing(102),  // WebDav - if part has been accepted and/or it might take a while

    OK(200),
    Accepted(202),
    // The request has been accepted for processing, but the processing has not been completed. The request might or might not be eventually acted upon, and may be disallowed when processing occurs.[10]
    NonAuthoritativeInformation(203),
    // The server is a transforming proxy (e.g. a Web accelerator) that received a 200 OK from its origin, but is returning a modified version of the origin's response.[11][12]
    NoContent(204), // The server successfully processed the request and is not returning any content.[13]
    ResetContent(205),
    // Like 204 but this response requires that the requester reset the document view.
    PartialContent(206),
    // The server is delivering only part of the resource (byte serving) due to a range header sent by the client. The range header is used by HTTP clients to enable resuming of interrupted downloads, or split a download into multiple simultaneous streams.[15]
    MultiStatus(207), // WebDAV
    // The message body is  XML message and may contain a number of separate response codes
    AlreadyReported(208), // WebDAV
    // The members of a DAV binding have already been enumerated in a preceding part of the (multistatus) response, so are omitted
    IMUsed(226),
    // The server has fulfilled a request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance.[17]


    MultipleChoices(300),
    // Indicates multiple options for the resource from which the client may choose (via agent-driven content negotiation). E.g. multiple video formats (file extensions) or word-sense disambiguation.
    MovedPermanently(301),
    // This and all future requests should be directed to the given URI.[20]
    Found(302), // Deprecated - sometimes used instead of 301
    SeeOther(303),
    // The response to the request can be found under another URI using the GET method. When received in response to a POST (or PUT/DELETE), the client should presume that the server has received the data and should issue a new GET request to the given URI.[24]
    NotModified(304),
    // Indicates that the resource has not been modified since the version specified by the request headers If-Modified-Since or If-None-Match. In such case, there is no need to retransmit the resource since the client still has a previously-downloaded copy.[25]
    UseProxy(305),
    //  The requested resource is available only through a proxy, the address for which is provided in the response. Many HTTP clients (such as Mozilla[26] and Internet Explorer) do not correctly handle responses with this status code, primarily for security reasons.[27]
    SwitchProxy(306),
    // No longer used. Originally meant "Subsequent requests should use the specified proxy."[28]
    TemporaryRedirect(307),
    // In this case, the request should be repeated with another URI; however, future requests should still use the original URI. In contrast to how 302 was historically implemented, the request method is not allowed to be changed when reissuing the original request. For example, a POST request should be repeated using another POST request.[29]
    PermanentRedirect(308),
    // The request and all future requests should be repeated using another URI. 307 and 308 parallel the behaviors of 302 and 301, but do not allow the HTTP method to change. So, for example, submitting a form to a permanently redirected resource may continue smoothly.[30]



    BadRequest(400),
    //  lient error (e.g., malformed request syntax, size too large, invalid request message framing, or deceptive request routing)
    Unauthorized(401),
    // when authentication is required and has failed or has not yet been provided.
    // The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource. See Basic access authentication and Digest access authentication.[33] 401 semantically means "unauthenticated",[34] i.e. the user does not have the necessary credentials.
    // Note: Some sites issue HTTP 401 when an IP address is banned from the website (usually the website domain) and that specific address is refused permission to access a website.
    PaymentRequired(402),
    // Google uses this if daily limit is exceeded...
    Forbidden(403),
    // The request was valid, but the server is refusing action- maybe lacks permission.
    NotFound(404),
    // Maybe bad URL, but it might be available in the future. More requests are permissible.
    MethodNotAllowed(405),
    // A request method is not supported for the resource, e.g., a GET when a POST works, or a PUT on a read-only resource.
    NotAcceptable(406),
    // Not acceptable (type) according to the Accept headers sent in the request.
    ProxyAuthenticationRequired(407),
    // The client must first authenticate itself with the proxy.[39]
    RequestTimeout(408),
    // The server timed out waiting for the request. The client can try again
    Conflict(409),
    // Could not be processed because of conflicts, such as multiple simultaneous updates.
    Gone(410),
    // The resource requested is no longer available and will not be available again.
    LengthRequired(411),
    // The request did not specify the length of its content, which is required by the requested resource.[42]
    PreconditionFailed(412),
    // The server does not meet one of the preconditions that the requester put on the request.[43]
    PayloadTooLarge(413),
    // The request is larger than the server is willing or able to process. Previously called "Request Entity Too Large".[44]
    URITooLong(414),
    // The URI provided was too long for the server to process. Often the result of too much data being encoded as a query-string of a GET request, in which case it should be converted to a POST request.[45] Called "Request-URI Too Long" previously.[46]
    UnsupportedMediaType(415),
    // The request entity has a media type which the server or resource does not support. For example, the client uploads an image as image/svg+xml, but the server requires that images use a different format.
    RangeNotSatisfiable(416),
    // The client has asked for a portion of the file (byte serving), but the server cannot supply that portion. For example, if the client asked for a part of the file that lies beyond the end of the file.[47] Called "Requested Range Not Satisfiable" previously.[48]
    ExpectationFailed(417),
    // The server cannot meet the requirements of the Expect request-header field.[49]
    ImATeapot(418),
    // This code was defined in 1998 as one of the traditional IETF April Fools' jokes, i Hyper Text Coffee Pot Control Protocol, and is not expected to be implemented by actual HTTP servers. Ththis code should be returned by teapots requested to brew coffee.[50] This HTTP status is used as an Easter egg in some websites, including Google.com.[51][52]
    MisdirectedRequest(421),
    // The request was directed at a server that is not able to produce a response.[53] (for example because of a connection reuse)[54]
    UnprocessableEntity(422),  // WebDav
    // The request was well-formed but was unable to be followed due to semantic errors.[16]
    Locked(423),  // WebDav
    // The resource that is being accessed is locked.[16]
    FailedDependency(424), // WebDav
    //  The request failed due to failure of a previous request (e.g., a PROPPATCH).[16]
    UpgradeRequired(426),
    // The client should switch to a different protocol such as TLS/1.0, given in the Upgrade header field.[55]
    PreconditionRequired(428),
    // The origin server requires the request to be conditional. Intended to prevent the 'lost update' problem, where a client GETs a resource's state, modifies it, and PUTs it back to the server, when meanwhile a third party has modified the state on the server, leading to a conflict."[56]
    TooManyRequests(429),
    // The user has sent too many requests in a given amount of time. Intended for use with rate-limiting schemes.[56]
    RequestHeaderFieldsTooLarge(431),
    // The server is unwilling to process the request because either an individual header field, or all the header fields collectively, are too large.[56]
    UnavailableForLegalReasons(451),
    // A server operator has received a legal demand to deny access to a resource or to a set of resources that includes the requested resource.[57] The code 451 was chosen as a reference to the novel Fahrenheit 451


    //5xx Server errors -- The server failed to fulfill a request.[58]
    // Response status codes beginning with the digit "5" indicate cases in which the server is aware that it has encountered an error or is otherwise incapable of performing the request. Except when responding to a HEAD request, the server should include an entity containing an explanation of the error situation, and indicate whether it is a temporary or permanent condition. Likewise, user agents should display any included entity to the user. These response codes are applicable to any request method.[59]

    InternalServerError(500),
    // A generic error message, given when an unexpected condition was encountered and no more specific message is suitable.[60]
    NotImplemented(501),
    // The server either does not recognize the request method, or it lacks the ability to fulfill the request. Usually this implies future availability (e.g., a new feature of a web-service API).[61]
    BadGateway(502),
    // The server was acting as a gateway or proxy and received an invalid response from the upstream server.[62]
    ServiceUnavailable(503),
    // The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state.[63]
    GatewayTimeout(504),
    // The server was acting as a gateway or proxy and did not receive a timely response from the upstream server.[64]
    HTTPVersionNotSupported(505),
    // The server does not support the HTTP protocol version used in the request.[65]
    VariantAlsoNegotiates(506),
    // Transparent content negotiation for the request results in a circular reference.[66]
    InsufficientStorage(507), // WebDav
    // The server is unable to store the representation needed to complete the request.[16]
    LoopDetected(508),  // WebDav
    // The server detected an infinite loop while processing the request (sent in lieu of 208 Already Reported).
    NotExtended(510),
    // Further extensions to the request are required for the server to fulfil it.[67]
    NetworkAuthenticationRequired(511),
    // The client needs to authenticate to gain network access. Intended for use by intercepting proxies used to control access to the network (e.g., "captive portals" used to require agreement to Terms of Service before granting full Internet access via a Wi-Fi hotspot).[56]
    // Unofficial codes[edit]
    // The following codes are not specified by any standard.


    // OTHERS

    EnhanceYourCalm(420), // (Twitter)
    // Returned by version 1 of the Twitter Search and Trends API when the client is being rate limited; versions 1.1 and later use the 429 Too Many Requests response code instead.[73]
    BlockedByWindowsParentalControls(450), // (Microsoft)
    // The Microsoft extension code indicated when Windows Parental Controls are turned on and are blocking access to the requested webpage.[74]
    InvalidToken(498),
    // Returned by ArcGIS for Server. Code 498 indicates an expired or otherwise invalid token.[75]
    TokenRequired(499),
    // Returned by ArcGIS for Server. Code 499 indicates that a token is required but was not submitted.[75]
    BandwidthLimitExceeded(509), // (Apache Web Server/cPanel)
    // The server has exceeded the bandwidth specified by the server administrator; this is often used by shared hosting providers to limit the bandwidth of customers.[76]
    SiteIsFrozen(530),
    // Used by the Pantheon web platform to indicate a site that has been frozen due to inactivity.[77]
    NetworkReadRimeoutError(598); // (Informal convention)
    // Used by some HTTP proxies to signal a network read timeout behind the proxy to a client in front of the proxy.[78]

    int value;
    HtmlERR(int val) {
        value = val;
    }

    /**
     * Sends the status code and the code's name
     */
    public void send(HttpServerResponse response) {
        response.setStatusCode(value).setStatusMessage(toString()).end();
    }

    /**
     * Sends the status code (if no message is wanted, or that has already been set).
     */
    public void sendJustCode(HttpServerResponse response) {
        response.setStatusCode(value).end();
    }


    /**
     * Sends the status code and the code's name and the passed text
     */
    public void sendPlus(HttpServerResponse response, String text) {
        response.setStatusCode(value).setStatusMessage(toString()+"; "+text).end();
    }

    /**
     * Sends the status code and the passed text
     */
    public void sendWith(HttpServerResponse response, String text) {
        response.setStatusCode(value).setStatusMessage(text).end();
    }
}
