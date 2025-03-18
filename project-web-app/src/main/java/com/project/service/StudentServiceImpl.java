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
import com.project.model.Projekt;
import com.project.model.Student;

@Service
public class StudentServiceImpl implements StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final RestClient restClient;

    public StudentServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private String getResourcePath() {
        return "/api/studenci";
    }

    private String getResourcePath(Integer id) {
        return String.format("%s/%d", getResourcePath(), id);
    }

    private String getResourcePathByNrIndeksu(String nrIndeksu) {
        return String.format("%s/nrIndeksu/%s", getResourcePath(), nrIndeksu);
    }

    @Override
    public Optional<Student> getStudent(Integer studentId) {
        String resourcePath = getResourcePath(studentId);
        logger.info("REQUEST -> GET {}", resourcePath);
        Student student = restClient
                .get()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(Student.class);
        if (student != null) {
            logger.info("Student found: {}", student);
            student.getProjekty().forEach(projekt -> logger.info("Project: {}", projekt));
        }
        return Optional.ofNullable(student);
    }

    @Override
    public Optional<Student> getStudentByNrIndeksu(String nrIndeksu) {
        String resourcePath = getResourcePathByNrIndeksu(nrIndeksu);
        logger.info("REQUEST -> GET {}", resourcePath);
        Student student = restClient
                .get()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(Student.class);
        return Optional.ofNullable(student);
    }

    @Override
    public Student setStudent(Student student) {
        if (student.getStudentId() != null) { // modyfikacja istniejącego studenta
            String resourcePath = getResourcePath(student.getStudentId());
            logger.info("REQUEST -> PUT {}", resourcePath);
            restClient
                    .put()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(student)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            return student;
        } else {
            String resourcePath = getResourcePath();
            logger.info("REQUEST -> POST {}", resourcePath);
            ResponseEntity<Void> response = restClient
                    .post()
                    .uri(resourcePath)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(student)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .toBodilessEntity();
            URI location = response.getHeaders().getLocation();

            logger.info("REQUEST (location) -> GET {}", location);
            return restClient
                    .get()
                    .uri(location)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new HttpException(res.getStatusCode(), res.getHeaders());
                    })
                    .body(Student.class);
        }
    }

    @Override
    public void deleteStudent(Integer studentId) {
        String resourcePath = getResourcePath(studentId);
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
    public Page<Student> getStudenci(Pageable pageable) {
        URI uri = ServiceUtil.getURI(getResourcePath(), pageable);
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    @Override
    public Page<Student> searchByNrIndeksu(String nrIndeksu, Pageable pageable) {
        URI uri = ServiceUtil
                .getUriComponent(getResourcePath(), pageable)
                .queryParam("nrIndeksu", nrIndeksu)
                .build().toUri();
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    @Override
    public Page<Student> searchByNazwisko(String nazwisko, Pageable pageable) {
        URI uri = ServiceUtil
                .getUriComponent(getResourcePath(), pageable)
                .queryParam("nazwisko", nazwisko)
                .build().toUri();
        logger.info("REQUEST -> GET {}", uri);
        return getPage(uri);
    }

    @Override
    public List<Projekt> getProjekty(Integer studentId) {
        String resourcePath = String.format("%s/%d/projekty", getResourcePath(), studentId);
        logger.info("REQUEST -> GET {}", resourcePath);
        return restClient
                .get()
                .uri(resourcePath)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new HttpException(res.getStatusCode(), res.getHeaders());
                })
                .body(new ParameterizedTypeReference<List<Projekt>>() {});
    }

    private Page<Student> getPage(URI uri) {
        return restClient.get()
                .uri(uri.toString())
                .retrieve()
                .body(new ParameterizedTypeReference<RestResponsePage<Student>>() {});
    }
}
