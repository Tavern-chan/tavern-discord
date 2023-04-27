package controller

import com.asm.tavern.discord.app.App
import model.Authorization
import org.jooq.JSON
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.ext.XLogger
import org.slf4j.ext.XLoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate


@RestController
class TavernApiController {
    private final String API_ENDPOINT_OAUTH_STRING = "https://discord.com/api/oauth2/token"
    private final String API_ENDPOINT_USER_STRING = "https://discord.com/api/users/@me"
    private final String API_ENDPOINT_GUILDS_STRING = "https://discord.com/api/users/@me/guilds"
    private final String HEADER_CONTENT_TYPE_KEY = "Content-Type"
    private final String HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded"
    private final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
    private final String BEARER_AUTHORIZATION_STRING = "Bearer "
    private String auth_token = ""


    private static final XLogger logger = XLoggerFactory.getXLogger(this.class)


    //userid
    /// users/@me  .user


    @PostMapping(value = "/api/guild", produces = "application/json")
    @ResponseBody
    List<Map<String, String>> guildHandler(@RequestBody final Authorization authorization){
        List<Map<String, String>> responseList = new ArrayList<>()
        if(auth_token != "" || getAuthorizationToken(authorization.discordCode)){
            try{
                RestTemplate restTemplate = new RestTemplate()

                // Set the request parameters
                MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>()



                // Set the request headers
                HttpHeaders headers = new HttpHeaders()
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                headers.add("Authorization", BEARER_AUTHORIZATION_STRING + auth_token)

                // Create the request entity
                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers)

                // Make the request and retrieve the response
                /**

                ResponseEntity<String> userResponseEntity = restTemplate.exchange(API_ENDPOINT_USER_STRING, HttpMethod.GET, requestEntity, String.class);

                JSONObject userResponseJSON = new JSONObject(userResponseEntity.getBody())
                String userId = userResponseJSON.get("id")

                **/
                ResponseEntity<String> guildResponseEntity = restTemplate.exchange(API_ENDPOINT_GUILDS_STRING, HttpMethod.GET, requestEntity, String.class)
                JSONArray guildResponseJSON = new JSONArray(guildResponseEntity.getBody())


                guildResponseJSON.collect().forEach(guildJSON -> {

                    if(App.TavernApiServer.getJda().getGuildById(guildJSON["id"].toString())){
                        Map<String, String> guild = new HashMap<>()
                        guild.put("id", guildJSON["id"].toString())
                        guild.put("name", guildJSON["name"].toString())
                        guild.put("icon", guildJSON["icon"].toString())
                        responseList.add(guild)
                    }
                })
                return responseList
            }
            catch (Exception e){
                logger.error("error getting guilds from discord api: " + e)
            }
        }
        return responseList


    }



    boolean getAuthorizationToken(String authorizationCode){
        try{
            RestTemplate restTemplate = new RestTemplate()

            // Set the request parameters
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>()





            parameters.add("client_id", App.TavernApiServer.getDiscordClientId())
            parameters.add("client_secret", App.TavernApiServer.getDiscordClientSecret())
            parameters.add("grant_type", GRANT_TYPE_AUTHORIZATION_CODE)
            parameters.add("code", authorizationCode)
            parameters.add("redirect_uri", App.TavernApiServer.getRedirectURI())

            // Set the request headers
            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)

            // Create the request entity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers)

            // Make the request and retrieve the response
            ResponseEntity<String> responseEntity = restTemplate.exchange(API_ENDPOINT_OAUTH_STRING, HttpMethod.POST, requestEntity, String.class)

            JSONObject responseJSON = new JSONObject(responseEntity.getBody())
            auth_token = responseJSON.get("access_token")
            true
        }
        catch (Exception e){
            logger.error("error getting authorization token from discord api:" + e)
            false
        }

    }

}
