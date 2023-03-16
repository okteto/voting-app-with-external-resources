package com.okteto.vote.controller;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thymeleaf.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.UUID;


@Controller
public class VoteController {
    private static final String OPTION_A_ENV_VAR = "OPTION_A";
    private static final String OPTION_B_ENV_VAR = "OPTION_B";
    private static final String FUNCTION_URL = System.getenv("FUNCTION_URL");

    private final Logger logger = LoggerFactory.getLogger(VoteController.class);

    @GetMapping("/")
    String index(@CookieValue(name = "voter_id", defaultValue = "") String voterId,
                 Model model,
                 HttpServletResponse response) {
        String voter = voterId;
        Vote v = new Vote();
        model.addAttribute("optionA", v.getOptionA());
        model.addAttribute("optionB", v.getOptionB());
        model.addAttribute("hostname", v.getHostname());
        model.addAttribute("vote", null);

        if (StringUtils.isEmpty(voter)) {
            voter = UUID.randomUUID().toString();
        }

        Cookie cookie = new Cookie("voter_id", voter);
        response.addCookie(cookie);

        return "index";
    }

    @PostMapping("/")
    String postForm(@CookieValue(name = "voter_id", defaultValue = "") String voterId,
                    @ModelAttribute Vote voteInput,
                    Model model,
                    HttpServletResponse response) {
        String voter = voterId;
        String vote = voteInput.getVote();
        Vote v = new Vote();
        model.addAttribute("optionA", v.getOptionA());
        model.addAttribute("optionB", v.getOptionB());
        model.addAttribute("hostname", v.getHostname());
        
        // We pass the vote received in the post request
        model.addAttribute("vote", vote);
        if (StringUtils.isEmpty(voter)) {
            voter = UUID.randomUUID().toString();
        }
        logger.info(String.format("vote received for '%s'!", vote));

        Cookie cookie = new Cookie("voter_id", voter);
        response.addCookie(cookie);
        postToFunction(voter, vote);
        return "index";
    }

    @PostMapping("/vote")
    @ResponseStatus(HttpStatus.OK)
    void postVote(@RequestBody Vote voteRequest)throws ParseException {
        
        String vote = voteRequest.getVote();
        logger.info(String.format("vote received for '%s'!", vote));
        postToFunction("api", vote);
    }

    void postToFunction(String voter, String vote) {
        String body = String.format("{\"voter\": \"%s\", \"vote\": \"%s\"}", voter, vote);
        RestTemplate rest = new RestTemplate();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("content-type", "application/json");
        headers.setAll(headersMap);
        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        rest.exchange(FUNCTION_URL, HttpMethod.POST, requestEntity, String.class);
    }

    public static class Vote {
        private String optionA = "Burritos";
        private String optionB = "Tacos";
        private String hostname = "unknown";
        private String vote;

        public String getOptionA() {
            String result = System.getenv(OPTION_A_ENV_VAR);
            return StringUtils.isEmpty(result) ? this.optionA : result;
        }

        public void setOptionA(String optionA) {
            this.optionA = optionA;
        }

        public String getOptionB() {
            String result = System.getenv(OPTION_B_ENV_VAR);
            return StringUtils.isEmpty(result) ? this.optionB : result;
        }

        public void setOptionB(String optionB) {
            this.optionB = optionB;
        }

        public String getHostname() {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return this.hostname;
            }
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getVote() {
            return vote;
        }

        public void setVote(String vote) {
            this.vote = vote;
        }
    }
}