package com.audino.service;

import com.audino.model.InteractionAlert;
import com.audino.model.Medication;
import com.audino.model.Patient;
import com.audino.model.Prescription;

import java.util.List;
import java.util.Map;

public interface InteractionCheckStrategy {

    List<InteractionAlert> check(Patient patient, Prescription prescription, Map<String, Object> rules, List<Medication> allMedications);

    String getStrategyName();
}