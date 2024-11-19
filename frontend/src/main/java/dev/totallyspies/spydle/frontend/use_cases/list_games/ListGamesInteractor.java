package dev.totallyspies.spydle.frontend.use_cases.list_games;

import dev.totallyspies.spydle.frontend.client.rest.WebClientService;
import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import dev.totallyspies.spydle.matchmaker.generated.model.ListGamesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class ListGamesInteractor implements ListGamesInputBoundary {

    @Autowired
    public WebClientService webClient;

    @Override
    public ListGamesOutputData execute() {
        Object response = webClient.getEndpoint("/list-games", ListGamesResponseModel.class);
        if (response instanceof ListGamesResponseModel responseModel) {
            return new ListGamesOutputData(responseModel.getRoomCodes());
        } else if (response instanceof ClientErrorResponse clientErrorModel) {
            return new ListGamesOutputData(new LinkedList<>());
        }
        return new ListGamesOutputData(new LinkedList<>());
    }

}