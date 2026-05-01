/***********************************************************************************************************************
*   Implementación del Videojuego Service del sistema XP-Up                                                            *
*                                                                                                                      *
*   Esta clase contiene la lógica asociada a la búsqueda de videojuegos mediante una API externa                      *
*                                                                                                                      *
*   Metodos principales:                                                                                               *
*       - Buscar videojuegos a partir de un texto                                                                      *
*                                                                                                                      *
*   Para ello, realiza llamadas HTTP a la API externa RAWG, procesa la respuesta y transforma los datos              *
*   al modelo definido en el API Contract del sistema                                                                  *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.dto.rawg.RawgGameDto;
import com.david.xpup.backend.dto.rawg.RawgSearchResponseDto;
import com.david.xpup.backend.exception.BadRequestException;
import com.david.xpup.backend.service.VideojuegoService;
import com.david.xpup.generated.model.ExternalGameResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class VideojuegoServiceImpl implements VideojuegoService {

    private final String apiKey;
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public VideojuegoServiceImpl(
            @Value("${rawg.api.key}") String apiKey,
            @Value("${rawg.api.base-url}") String baseUrl
    ) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    //Busca videojuegos mediante la API externa RAWG -> GET /api/games/search
    @Override
    public List<ExternalGameResponse> searchGames(String query) {
        //Validamos que el texto de búsqueda tenga contenido suficiente
        if (query == null || query.trim().length() < 2) {
            throw new BadRequestException("Search text must have at least 2 characters.");
        }

        //Construimos la URL de búsqueda contra RAWG
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/games")
                .queryParam("key", apiKey)
                .queryParam("search", query.trim())
                .toUriString();

        //Llamamos a la API externa y parseamos su respuesta
        RawgSearchResponseDto rawgResponse = restTemplate.getForObject(url, RawgSearchResponseDto.class);

        if (rawgResponse == null || rawgResponse.getResults() == null) {
            return Collections.emptyList();
        }

        //Transformamos la respuesta externa al modelo del API Contract
        return rawgResponse.getResults().stream()
                .filter(this::hasValidRequiredFields)
                .map(this::toExternalGameResponse)
                .toList();
    }

    //Comprueba que el juego devuelto por RAWG tiene la información mínima necesaria para nuestra API
    private boolean hasValidRequiredFields(RawgGameDto rawgGame) {
        return rawgGame.getId() != null
                && rawgGame.getName() != null
                && !rawgGame.getName().isBlank();
    }

    //Transforma un juego de RAWG al DTO externo definido en el API Contract
    private ExternalGameResponse toExternalGameResponse(RawgGameDto rawgGame) {
        ExternalGameResponse response = new ExternalGameResponse();
        response.setIdJuegoApi(String.valueOf(rawgGame.getId()));
        response.setNombreJuego(rawgGame.getName());
        response.setPortadaJuegoUrl(rawgGame.getBackgroundImage());

        if (rawgGame.getReleased() != null && !rawgGame.getReleased().isBlank()) {
            response.setFechaLanzamiento(LocalDate.parse(rawgGame.getReleased()));
        }

        return response;
    }
}