package com.project.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.project.exception.HttpException;
import com.project.model.Zadanie;

@Service
public class ZadanieServiceImpl implements ZadanieService {
    private static final Logger logger = LoggerFactory.getLogger(ZadanieServiceImpl.class);

    private final RestClient restClient;

    public ZadanieServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private String getResourcePath() {
        return "/api/zadania";
    }

    private String getResourcePath(Integer id) {
        return String.format("%s/%d", getResourcePath(), id);
    }

    private String getResourcePathForProjekt(Integer projektId) {
        return String.format("/api/projekty/%d/zadania", projektId);
    }

    @Override
    public Optional<Zadanie> getZadanie(Integer zadanieId) {
        String resourcePath = getResourcePath(zadanieId);
        logger.info("REQUEST -> GET {}", resourcePath);
        Zadanie zadanie = restClient
                .get()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(Zadanie.class);
        return Optional.ofNullable(zadanie);
    }

    @Override
    public Zadanie setZadanie(Zadanie zadanie) {
        if (zadanie.getZadanieId() != null) { // modyfikacja istniejącego zadania
            String resourcePath = getResourcePath(zadanie.getZadanieId());
            logger.info("REQUEST -> PUT {}", resourcePath);
            restClient
                    .put()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(zadanie)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            return zadanie;
        } else {
            String resourcePath = getResourcePath();
            logger.info("REQUEST -> POST {}", resourcePath);
            ResponseEntity<Void> response = restClient
                    .post()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(zadanie)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            URI location = response.getHeaders().getLocation();

            logger.info("REQUEST (location) -> GET {}", location);
            return restClient
                    .get()
                    .uri(location.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .body(Zadanie.class);
        }
    }

    @Override
    public void deleteZadanie(Integer zadanieId) {
        String resourcePath = getResourcePath(zadanieId);
        logger.info("REQUEST -> DELETE {}", resourcePath);
        restClient
                .delete()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .toBodilessEntity();
    }

    @Override
    public Page<Zadanie> getZadaniaProjektu(Integer projektId, Pageable pageable) {
        URI uri = ServiceUtil.getURI(getResourcePathForProjekt(projektId), pageable);
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    @Override
    public List<Zadanie> getZadaniaProjektu(Integer projektId) {
        String resourcePath = getResourcePathForProjekt(projektId);
        logger.info("REQUEST -> GET {}", resourcePath);
        return restClient
                .get()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(new ParameterizedTypeReference<List<Zadanie>>() {});
    }

    @Override
    public Page<Zadanie> getZadania(Pageable pageable) {
        URI uri = ServiceUtil.getURI(getResourcePath(), pageable);
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    private Page<Zadanie> getPage(URI uri) {
        return restClient.get()
                .uri(uri.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<RestResponsePage<Zadanie>>() {});
    }
}
