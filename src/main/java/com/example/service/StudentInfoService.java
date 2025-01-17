package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.ResultCode;
import com.example.dao.AdminInfoDao;
import com.example.dao.CollegeInfoDao;
import com.example.dao.CourseSelectInfoDao;
import com.example.dao.StudentInfoDao;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.util.DateTimeUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class StudentInfoService {

    @Autowired
    private StudentInfoDao studentInfoDao;
    @Autowired
    private CollegeInfoDao collegeInfoDao;
    @Autowired
    private CourseSelectInfoDao courseSelectInfoDao;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    public Account login(String name, String password) {

        StudentInfo studentInfo = studentInfoDao.findByNameAndPassword(name, password);
        if (ObjectUtil.isEmpty(studentInfo)){
            throw new CustomException("-1","用户名、密码或者角色错误");
        }
        return studentInfo;
    }

//    public void register(StudentInfo studentInfo) {
//        StudentInfo info = studentInfoDao.findByName(studentInfo.getName());
//        if (ObjectUtil.isNotEmpty(info)){
//            throw new CustomException(ResultCode.USER_EXIST_ERROR);
//        }
//        studentInfoDao.insertSelective(studentInfo);
//    }

    public StudentInfo findById(Long id) {
        return studentInfoDao.selectByPrimaryKey(id);
    }

    public void update(StudentInfo studentInfo) {
        studentInfoDao.updateByPrimaryKeySelective(studentInfo);
    }

    public List<StudentInfo> findAll() {
        List<StudentInfo> list = studentInfoDao.selectAll();
        for (StudentInfo studentInfo:
             list) {
            if (ObjectUtil.isNotEmpty(studentInfo.getCollegeID())){
                CollegeInfo collegeInfo = collegeInfoDao.selectByPrimaryKey(studentInfo.getCollegeID());
                studentInfo.setCollegeName(collegeInfo.getName());
            }
        }
        return list;
    }

    public void add(StudentInfo studentInfo) {
        StudentInfo info = studentInfoDao.findByName(studentInfo.getName());
        if (ObjectUtil.isNotEmpty(info)){
            throw new CustomException(ResultCode.USER_EXIST_ERROR);
        }
        if (ObjectUtil.isEmpty(studentInfo.getPassword())){
            studentInfo.setPassword("123456");
        }
        studentInfoDao.insertSelective(studentInfo);
    }

    public void deleteById(Long id) {
        studentInfoDao.deleteByPrimaryKey(id);
    }

    public PageInfo<StudentInfo> findPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<StudentInfo> infos = studentInfoDao.findAll();
        return PageInfo.of(infos);
    }

    public PageInfo<StudentInfo> findPageName(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum,pageSize);
        List<StudentInfo> infos = studentInfoDao.findByNamePage(name);
        return PageInfo.of(infos);
    }

    public void exportCourseTable() {
        Account user = (Account) request.getSession().getAttribute("user");
        Long userId = user.getId();
        List<CourseSelectInfo> courseSelectInfos = courseSelectInfoDao.findByStudentId(userId);
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/课程表模板.xlsx");

        try {
            assert in != null;
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            for (CourseSelectInfo c :
                    courseSelectInfos) {
                int dayOfWeek = DateTimeUtil.getDayOfWeek(c.getTime());
                int timeSlot = DateTimeUtil.getTimeSlot(c.getTime());
                Row row = sheet.getRow(timeSlot);
                Cell cell = row.getCell(dayOfWeek + 2); // +2 because weekdays start from column 3

                cell.setCellValue(c.getName() + "\n" + c.getTeacherName() + "\n" + c.getLocation());

                // Merge two cells vertically
                sheet.addMergedRegion(new CellRangeAddress(timeSlot, timeSlot + 1, dayOfWeek + 2, dayOfWeek + 2));
            }

            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
