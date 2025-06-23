package com.tinyinventory.app.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

    // *** Whenever we send a request we also need to send the token that was generated on Login and saved in local storage ***
    // *** When using Postman we have to add it at Authorization tab, select Bearer, and paste the token ***


@Slf4j //Used fot log. method
@Service
public class JwtService {

    // **************** Token Generation ******************

    //This is a hardcoded secretKey for getKey() method. I don't use it, is just for example
    private static final String SECRET = "TmV3U2VjcmV0S2V5Rm9ySldUU2lnbmluZ1B1cnBvc2VzMTIzNDU2Nzg=\r\n";

    private final String secretKey;

    public JwtService(){
        secretKey = generateSecretKey();
    }

    public String generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGen.generateKey();
            System.out.println("Secret Key : " + secretKey.toString());
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating secret key", e);
        }
    }

    //We are using two libraries (maven dependencies to create the toke): JJWT :: API and JJWT :: Impl
    //And, because sometimes if it doesn't work we also have to add another dependency: JJWT :: Extensions :: Jackson
    public String generateToken(String username) {

        //JWT Payload Data (claims)
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .setClaims(claims) // Claims (JwtToken Payload Data): username, issues date, exparation (the 3 lines below)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*24*7)) //7 days OR Comment line for No Expiration Date
                .signWith(getKey(), SignatureAlgorithm.HS256).compact(); //here we have to sign the token using an algorithm

        //Because the methods from above are deprecated maybe we should try with a new approach
        //https://javadoc.io/doc/io.jsonwebtoken/jjwt-api/0.12.2/io/jsonwebtoken/JwtBuilder.html#setClaims(java.util.Map)
        //Not sure if the code below works
/*
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000*60*3))
                .signWith(getKey(), Jwts.SIG.HS256).compact()
                .build();*/
    }

    //Generates Key to sign the JWT Token
    public Key getKey() {
        //you can use a HARDCODED String Key
        //or you can use a special design method that will generate one: generateSecretKey()
        byte[] keyByte = Decoders.BASE64.decode(secretKey);
        //we use hmacShaKeyFor() method to generate the key
        return Keys.hmacShaKeyFor(keyByte);
    }

    // **************** Token Generation ******************

    public String extractUserName(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
      /*  return Jwts.parserBuilder() //parserBuilder() method not found
                .setSigningKey(getKey())
                .build().parseClaimsJws(token).getBody(); */
      /*  return Jwts
                .parser()
                .verifyWith(getKey()) // problem with this method
                .build()
                .parseSignedClaims(token)
                .getPayload();*/

        //PREVIOUS -> didn't catch invalid token waning
        return Jwts.parser() //parserBuilder() method not found
                .setSigningKey(getKey())
                .build().parseClaimsJws(token).getBody();

        //NEW version -> trying to reduce the stacktrace errors -- NEEDS WORK
        /*try {
            return Jwts.parser() //parserBuilder() method not found
                    .setSigningKey(getKey())
                    .build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            // Log and rethrow or return null
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token");
        }*/
    }


    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        //checks if the username from the database is equal to the username from the JwtToken, and if the JwtToken is expired
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


}
