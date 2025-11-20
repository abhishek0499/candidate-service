package com.abhishek.candidateService.repository;

import com.abhishek.candidateService.model.Attempt;
import com.abhishek.candidateService.model.Status;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface AttemptRepository extends MongoRepository<Attempt, String> {
    List<Attempt> findByCandidateIdAndStatus(String candidateId, Status status);

    Optional<Attempt> findByIdAndCandidateId(String id, String candidateId);

    List<Attempt> findByStatus(Status status);
}