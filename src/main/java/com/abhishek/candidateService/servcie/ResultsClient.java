package com.abhishek.candidateService.servcie;

import com.abhishek.candidateService.model.Attempt;
import org.springframework.stereotype.Component;

@Component
public class ResultsClient {
// In production call Results Service endpoint /results/evaluate
public void evaluateAttempt(Attempt attempt) {
// stub: in prod send HTTP request
System.out.println("[ResultsClient] evaluating attempt " + attempt.getId());
}
}