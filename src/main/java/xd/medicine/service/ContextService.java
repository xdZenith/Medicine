package xd.medicine.service;

import xd.medicine.entity.dto.EmergencyAttr;
import xd.medicine.entity.dto.PatientEmergency;

import java.util.List;

/**
 * created by xdCao on 2017/12/21
 */

public interface ContextService {

    double TEMPERATURE_LIMIT_HIGH=37.5;
    double TEMPERATURE_LIMIT_LOW=36.0;
    int HEARTBEAT_LIMIT_HIGH=100;
    int HEARTBEAT_LIMIT_LOW=60;
    double BLOOD_PPRESSURE_LIMIT_HIGH=140.0;
    double BLOOD_PRESSURE_LIMIT_LOW=60.0;


    List<PatientEmergency> getAllPatientsWithEmerg();

    EmergencyAttr getEmergencyAttrPeriodly();

}
