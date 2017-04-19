/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infoadminsys.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hed
 */

public class GradeUtil {

    private JdbcUtil jdbcUtil;

    public GradeUtil() {
        jdbcUtil = new JdbcUtil();
        jdbcUtil.getConnection();
    }

    public boolean saveGrade(Map<String,Object> gradeMap) {
        String sql = "insert into grade(course_id,student_id,score) values(?,?,?)";
        List<Object> param = new ArrayList<>();
        param.add(gradeMap.get("courseId"));
        param.add(gradeMap.get("id"));
        param.add(gradeMap.get("score"));
        boolean flag = false;
        try {
            flag = jdbcUtil.updateByPreparedStatement(sql,param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public List<Map<String, Object>> getGradesByStudentId(int studentId) {

        String sql = "select c.id as id, c.name, s.name, sc.score from course as c, " +
                "selectedcourse as sc where sc.student_id = ? and sc.course_id = c.ids";
        List<Object> param = new ArrayList<>();
        param.add(studentId);
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            list = jdbcUtil.getCertainResultList(sql,param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        if (jdbcUtil != null) {
            jdbcUtil.releaseConn();
            jdbcUtil = null;

        }
        System.out.println(this.getClass().toString() + "销毁了");
    }
    
}
