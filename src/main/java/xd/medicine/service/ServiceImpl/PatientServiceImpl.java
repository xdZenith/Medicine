package xd.medicine.service.ServiceImpl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xd.medicine.dao.autoMapper.PatientMapper;
import xd.medicine.entity.bo.*;
import xd.medicine.entity.dto.PatientForFront;
import xd.medicine.entity.dto.PatientWithTrust;
import xd.medicine.service.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * created by xdCao on 2017/12/19
 */
@Service
public class PatientServiceImpl implements PatientService{

    private static final Logger LOGGER= LoggerFactory.getLogger(PatientServiceImpl.class);


    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private TrustAttrService trustAttrService;
    @Autowired
    private ProDutyLogService proDutyLogService;
    @Autowired
    private PostDutyLogService postDutyLogService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer insertPatient(Patient patient) {
        return patientMapper.insert(patient);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePatient(int id) {
        proDutyLogService.deleteProDutyLogByPatient(id);
        postDutyLogService.deletePostDutyLogByPatient(id);
        return patientMapper.deleteByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer updatePatient(Patient patient) {
        return patientMapper.updateByPrimaryKeySelective(patient);
    }

    @Override
    public List<PatientWithTrust> getAllPatients() {
        List<Patient> patients = patientMapper.selectByExample(new PatientExample());
        List<PatientWithTrust> patientWithTrusts = getPatientWithTrusts(patients);
        return patientWithTrusts;
    }

    @Override
    public PatientWithTrust getPatientById(int id) {
        Patient patient = patientMapper.selectByPrimaryKey(id);
        TrustAttr trustAttr=trustAttrService.getTrustAttrById(patient.getTrustattrId());
        PatientWithTrust patientWithTrust=new PatientWithTrust(patient,trustAttr);
        return patientWithTrust;

    }

    @Override
    public PageInfo<PatientWithTrust> getPatientByPage(int page, int rows) {
        PageHelper.startPage(page,rows);
        List<Patient> patients = patientMapper.selectByExample(new PatientExample());
        List<PatientWithTrust> patientWithTrusts = getPatientWithTrusts(patients);
        PageInfo<PatientWithTrust> patientPageInfo=new PageInfo<>(patientWithTrusts);
        return patientPageInfo;
    }

    @Override
    public PageInfo<PatientForFront> getPatientByPage2(int page, int rows, int doctorId) {
        PageHelper.startPage(page,rows);
        List<Patient> patients = patientMapper.selectByExample(new PatientExample());
        List<PatientWithTrust> patientWithTrusts = getPatientWithTrusts(patients);
        List<PatientForFront> patientForFronts = getPatientForFront(patientWithTrusts,doctorId);
        PageInfo<PatientForFront> patientPageInfo=new PageInfo<>(patientForFronts);
        return patientPageInfo;
    }

    @Override
    public List<PatientWithTrust> getPatientsByDoctor(int doctorId) {
        PatientExample patientExample=new PatientExample();
        patientExample.createCriteria().andDoctorIdEqualTo(doctorId);
        List<Patient> patients = patientMapper.selectByExample(patientExample);
        List<PatientWithTrust> patientWithTrusts = getPatientWithTrusts(patients);
        return patientWithTrusts;
    }

    @Override
    public Integer countPatientByAccount(String account) {
        PatientExample example=new PatientExample();
        example.createCriteria().andAccountEqualTo(account);
        return patientMapper.countByExample(example);
    }

    @Override
    public List<PatientWithTrust> getPatientByAccount(String account) {
        PatientExample example=new PatientExample();
        example.createCriteria().andAccountEqualTo(account);
        List<Patient> patients = patientMapper.selectByExample(example);
        List<PatientWithTrust> patientWithTrusts = getPatientWithTrusts(patients);
        return patientWithTrusts;
    }


    private List<PatientWithTrust> getPatientWithTrusts(List<Patient> patients) {
        List<PatientWithTrust> patientWithTrusts=new ArrayList<>();
        for (Patient patient:patients){
            TrustAttr attr = trustAttrService.getTrustAttrById(patient.getTrustattrId());
            PatientWithTrust patientWithTrust=new PatientWithTrust(patient,attr);
            patientWithTrusts.add(patientWithTrust);
        }
        return patientWithTrusts;
    }


    private List<PatientForFront> getPatientForFront(List<PatientWithTrust> patients , int doctorId) {
        List<PatientForFront> patientForFronts=new ArrayList<>();
        for (PatientWithTrust patient:patients){
            boolean isMyPatient = (patient.getPatient().getDoctorId()==doctorId);
            PatientForFront patientForFront = new PatientForFront(patient, isMyPatient);
            patientForFronts.add(patientForFront);
        }
        return patientForFronts;
    }

    @Override
    public Integer count() {
        return patientMapper.countByExample(new PatientExample());
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public Patient updateEmergency(Integer patientId, Double temperature, Integer heartBeat, Double bloodPressure) {
        Patient patient = patientMapper.selectByPrimaryKey(patientId);
        patient.setTemperature(temperature);
        patient.setBloodPressure(bloodPressure);
        patient.setHeartBeat(heartBeat);
        patient.setIsInEmergency(judgeEmergency(temperature,heartBeat,bloodPressure));
        if (patient.getIsInEmergency()&&patient.getSenseAware()){
            patient.setEmergtime(new Date());
        }
        patientMapper.updateByPrimaryKeySelective(patient);
        return patient;
    }


    public Boolean judgeEmergency(Double temperature, Integer heartBeat, Double bloodPressure) {
        if (temperature<=TEMPERATURE_LIMIT_HIGH&&temperature>=TEMPERATURE_LIMIT_LOW)
            if (heartBeat<=HEARTBEAT_LIMIT_HIGH&&heartBeat>=HEARTBEAT_LIMIT_LOW)
                if (bloodPressure<=BLOOD_PPRESSURE_LIMIT_HIGH&&bloodPressure>=BLOOD_PRESSURE_LIMIT_LOW)
                    return false;
        return true;
    }

    /* 获得满足科室要求的所有医生，即候选主体集合SIS */
    @Override
    public List<Doctor> getSisDoctorsByPatientId(int patientId){
        PatientWithTrust patient = getPatientById(patientId);
        TrustAttr trustAttr = patient.getTrustAttr();
        List<Doctor> doctorList = doctorService.getDoctorByDepartment(trustAttr.getDepartment());
        return  doctorList;
    }

    @Override
    public List<PatientWithTrust> sensePatientInEmergency() {
        PatientExample example=new PatientExample();
        example.createCriteria().andSenseAwareEqualTo(true).andIsInEmergencyEqualTo(true);
        List<Patient> patients = patientMapper.selectByExample(example);
        return getPatientWithTrusts(patients);
    }


}
