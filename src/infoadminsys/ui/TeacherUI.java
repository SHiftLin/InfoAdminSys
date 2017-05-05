/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infoadminsys.ui;

import infoadminsys.cls.*;
import infoadminsys.util.*;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.*;

/**
 *
 * @author hed
 */
public class TeacherUI extends javax.swing.JFrame {

    private static JFrame frame;

    /**
     * Creates new form TeacherUI
     */
    private String id;
    private boolean sudo;
    private Teacher teacher;
    private TeacherUtil teacherUtil = new TeacherUtil();

    private GradeInputModel gradeInputModel;
    private CourseInfoModel courseInfoModel;

    private void setText() {
        jLabel_hello.setText("您好, " + teacher.name);
        jTextField_name.setText(teacher.name);
        jTextField_sex.setText(teacher.sex);
        jComboBox_sex.setSelectedItem(teacher.sex);
        jTextField_id.setText(teacher.id);
        jTextField_depart.setText(teacher.depart);
        jTextField_title.setText(teacher.title);
        jTextField_hometown.setText(teacher.hometown);
        if (teacher.birthday != null) {
            jTextField_birthday.setText(teacher.birthday.toString());
        } else {
            jTextField_birthday.setText("");
        }
        jTextField_IDnum.setText(teacher.IDnum);
        jTextField_address.setText(teacher.address);
        jTextField_email.setText(teacher.email);
        jTextField_cell.setText(teacher.cell);
    }

    private void setEdit(boolean edit) {
        jTextField_name.setEditable(edit & sudo);
        jTextField_sex.setEditable(edit);
        jTextField_sex.setVisible(!edit);
        jComboBox_sex.setVisible(edit);
        jTextField_id.setEditable(false);
        jTextField_depart.setEditable(edit & sudo);
        jTextField_title.setEditable(edit & sudo);
        jTextField_hometown.setEditable(edit);
        jTextField_birthday.setEditable(edit);
        jTextField_IDnum.setEditable(edit & sudo);
        jTextField_address.setEditable(edit);;
        jTextField_email.setEditable(edit);
        jTextField_cell.setEditable(edit);
    }

    private void buttonVisible(boolean vis) {
        jButton_modify.setVisible(!vis);
        jButton_save.setVisible(vis);
        jButton_back.setVisible(vis);
    }

    private void setVis(boolean vis) {
        jLabel_hello.setVisible(vis);
        jLabel_account.setVisible(vis);
        jLabel_logOut.setVisible(vis);
    }

    private void saveData() throws ParseException, Exception {
        teacher.name = jTextField_name.getText();
        teacher.sex = jTextField_sex.getText();
        teacher.id = jTextField_id.getText();
        teacher.depart = jTextField_depart.getText();
        teacher.title = jTextField_title.getText();
        teacher.hometown = jTextField_hometown.getText();
        teacher.birthday = Utility.StringToDate(jTextField_birthday.getText());
        teacher.address = jTextField_address.getText();
        teacher.IDnum = jTextField_IDnum.getText();
        teacher.email = jTextField_email.getText();
        teacher.cell = jTextField_cell.getText();
        teacherUtil.uploadData(teacher);
    }

    private void displayInfo(boolean readDB) {
        if (readDB) {
            teacher = teacherUtil.downloadData(id);
        }
        setText();
        setEdit(false);
        buttonVisible(false);
        setVis(!sudo);
    }

    private void adjust() {
        DefaultTableCellRenderer hr=(DefaultTableCellRenderer) jTable_courses.getTableHeader().getDefaultRenderer();
        hr.setHorizontalAlignment(JLabel.CENTER);
        jTable_courses.getTableHeader().setFont(new Font("Lucida Grande", 0, 13));
        jTable_courses.getTableHeader().setDefaultRenderer(hr);
        jTable_scores.getTableHeader().setFont(new Font("Lucida Grande", 0, 13));
        jTable_scores.getTableHeader().setDefaultRenderer(hr);
    }

    public TeacherUI() {
    }

    public TeacherUI(String username, boolean status) {
        id = username;
        sudo = status;
        initComponents();
        displayInfo(true);

        courseInfoModel = new CourseInfoModel();
        jTable_courses.setModel(courseInfoModel);
        gradeInputModel = new GradeInputModel();
        jTable_scores.setModel(gradeInputModel);

        jTable_courses.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String courseId = courseInfoModel.getValueAt(jTable_courses.getSelectedRow(), 0).toString().trim();            
                gradeInputModel.setStudentByCourseId(courseId);
                jTable_scores.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                super.mouseClicked(e);
            }
        });
        adjust();

    }

    private class CourseInfoModel extends AbstractTableModel {

        private CourseUtil courseUtil = new CourseUtil();
        private List<Map<String, Object>> list = getAllCourses();

        String[] columnStrings = {"id", "name", "status"};
        String[] columnShowStrings = {"课程编号", "课程名", "提交状态"};

        public void update() {
            list = getAllCourses();
            fireTableDataChanged();
        }

        private List<Map<String, Object>> getAllCourses() {
            return courseUtil.findAllCoursesByTeacherId(teacher.id);
        }

        public int getRowCount() {
            return list.size();
        }

        public int getColumnCount() {
            return columnStrings.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Map<String, Object> map = list.get(rowIndex);
            return map.get(columnStrings[columnIndex]);
        }

        public String getColumnName(int column) {
            return columnShowStrings[column];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    private class GradeInputModel extends AbstractTableModel {

        private String courseId;

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        private CourseUtil courseUtil = new CourseUtil();
        private GradeUtil gradeUtil = new GradeUtil();
        private List<Map<String, Object>> list = new ArrayList<>();

        String[] columnStrings = {"id", "course_id", "name", "score"};
        String[] columnShowStrings = {"学号", "课程编号", "姓名", "成绩"};

        boolean[] canEdit = new boolean[]{
            false, false, false, true
        };

        public List<Map<String, Object>> getAllStudentByCourseId(String courseId) {
            return courseUtil.findAllStudentWithGradeByCourseId(courseId);
        }

        public boolean commitGrades() {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                if (map.get("score") == null || map.get("score") == "") {
                    JOptionPane.showMessageDialog(frame, "请将成绩填写完整后再提交", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            }

            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                gradeUtil.saveGrade(map);
                courseUtil.commitCourseByCourseId(courseId);
            }
            return true;
        }

        public void setStudentByCourseId(String courseId) {
            this.courseId = courseId;
            list = getAllStudentByCourseId(courseId);
            fireTableDataChanged();
        }

        public int getRowCount() {
            return list.size();
        }

        public int getColumnCount() {
            return columnShowStrings.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Map<String, Object> map = list.get(rowIndex);
            return map.get(columnStrings[columnIndex]);
        }

        public String getColumnName(int column) {
            return columnShowStrings[column];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == columnShowStrings.length - 1;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Map<String, Object> map = list.get(rowIndex);
            map.put(columnStrings[columnIndex], aValue);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel_name = new javax.swing.JLabel();
        jTextField_name = new javax.swing.JTextField();
        jLabel_id = new javax.swing.JLabel();
        jTextField_id = new javax.swing.JTextField();
        jLabel_title = new javax.swing.JLabel();
        jTextField_title = new javax.swing.JTextField();
        jButton_save = new javax.swing.JButton();
        jButton_back = new javax.swing.JButton();
        jButton_modify = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel_birthday = new javax.swing.JLabel();
        jTextField_birthday = new javax.swing.JTextField();
        jLabel_email = new javax.swing.JLabel();
        jTextField_email = new javax.swing.JTextField();
        jLabel_hometown = new javax.swing.JLabel();
        jTextField_hometown = new javax.swing.JTextField();
        jLabel_cell = new javax.swing.JLabel();
        jTextField_cell = new javax.swing.JTextField();
        jLabel_depart = new javax.swing.JLabel();
        jTextField_depart = new javax.swing.JTextField();
        jComboBox_sex = new javax.swing.JComboBox<>();
        jLabel_address = new javax.swing.JLabel();
        jTextField_address = new javax.swing.JTextField();
        jTextField_sex = new javax.swing.JTextField();
        jLabel_IDnum = new javax.swing.JLabel();
        jTextField_IDnum = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_scores = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable_courses = new javax.swing.JTable();
        saveButton = new javax.swing.JButton();
        jLabel_account = new javax.swing.JLabel();
        jLabel_hello = new javax.swing.JLabel();
        jLabel_logOut = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jTabbedPane1.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(800, 600));
        jTabbedPane1.setRequestFocusEnabled(false);

        jPanel1.setPreferredSize(new java.awt.Dimension(800, 522));

        jLabel_name.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_name.setText("姓名：");

        jTextField_name.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jTextField_name.setToolTipText("");

        jLabel_id.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_id.setText("工号：");

        jTextField_id.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jLabel_title.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_title.setText("职称：");

        jTextField_title.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jButton_save.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jButton_save.setText("保存");
        jButton_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_saveActionPerformed(evt);
            }
        });

        jButton_back.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jButton_back.setText("返回");
        jButton_back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_backActionPerformed(evt);
            }
        });

        jButton_modify.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jButton_modify.setText("修改");
        jButton_modify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_modifyActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel1.setText("性别：");

        jLabel_birthday.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_birthday.setText("出生日期：");

        jTextField_birthday.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jTextField_birthday.setToolTipText("");

        jLabel_email.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_email.setText("e-mail：");

        jTextField_email.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jLabel_hometown.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_hometown.setText("籍贯：");

        jTextField_hometown.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jLabel_cell.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_cell.setText("联系电话：");

        jTextField_cell.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jLabel_depart.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_depart.setText("院系：");

        jTextField_depart.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jComboBox_sex.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "男", "女" }));
        jComboBox_sex.setEditor(null);

        jLabel_address.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_address.setText("地址：");

        jTextField_address.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        jTextField_sex.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jTextField_sex.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel_IDnum.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel_IDnum.setText("身份证号码：");

        jTextField_IDnum.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton_modify)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_save)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_back)
                .addGap(20, 20, 20))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_email, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_address, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_hometown, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_id, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_name, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField_hometown, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField_id, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField_name, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel_depart, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel_birthday, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jTextField_sex, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jComboBox_sex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jTextField_birthday, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel_IDnum))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jTextField_depart, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(28, 28, 28)
                                            .addComponent(jLabel_title)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField_title, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField_IDnum, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addComponent(jTextField_address, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 678, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField_email, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jLabel_cell)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_cell, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_name)
                    .addComponent(jLabel1)
                    .addComponent(jTextField_sex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox_sex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_id)
                    .addComponent(jTextField_id, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_depart)
                    .addComponent(jTextField_depart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_title)
                    .addComponent(jTextField_title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_hometown)
                    .addComponent(jTextField_hometown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_birthday)
                    .addComponent(jTextField_birthday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_IDnum)
                    .addComponent(jTextField_IDnum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_address)
                    .addComponent(jTextField_address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_email)
                    .addComponent(jTextField_email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_cell)
                    .addComponent(jTextField_cell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 327, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton_back)
                    .addComponent(jButton_modify)
                    .addComponent(jButton_save))
                .addGap(12, 12, 12))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel_email, jTextField_email});

        jTextField_birthday.getAccessibleContext().setAccessibleName("");

        jTabbedPane1.addTab("个人信息", jPanel1);

        jPanel2.setPreferredSize(new java.awt.Dimension(800, 576));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(800, 576));

        jTable_scores.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        jTable_scores.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable_scores.setGridColor(new java.awt.Color(102, 102, 102));
        jTable_scores.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable_scores);

        jTable_courses.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        jTable_courses.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTable_courses.getTableHeader().setReorderingAllowed(false);
        jTable_courses.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_coursesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable_courses);

        saveButton.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        saveButton.setText("保存");
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveButtonMouseClicked(evt);
            }
        });
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 3, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveButton)
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(saveButton)
                .addGap(12, 12, 12))
        );

        jTabbedPane1.addTab("课程查询／成绩录入", jPanel2);

        jLabel_account.setText("账号管理");
        jLabel_account.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel_account.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_accountMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel_accountMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel_accountMouseEntered(evt);
            }
        });

        jLabel_hello.setText("您好,");

        jLabel_logOut.setText("注销");
        jLabel_logOut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel_logOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_logOutMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel_logOutMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel_logOutMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel_hello)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel_account, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_logOut, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel_hello, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_account, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_logOut))
                .addGap(0, 0, 0)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel_account, jLabel_logOut});

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel_accountMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_accountMouseClicked
        // TODO add your handling code here:
        setEnabled(false);
        AccountManageUI AM = new AccountManageUI(teacher.id, teacher.type, false);
        AM.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        AM.setLocationRelativeTo(this);
        AM.setAlwaysOnTop(true);
        AM.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                setEnabled(true);
            }
        });
        AM.setVisible(true);
    }//GEN-LAST:event_jLabel_accountMouseClicked

    private void jLabel_accountMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_accountMouseEntered
        // TODO add your handling code here:
        jLabel_account.setText("<html><u><b>账号管理</b></u></html>");
    }//GEN-LAST:event_jLabel_accountMouseEntered

    private void jLabel_accountMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_accountMouseExited
        // TODO add your handling code here:
        jLabel_account.setText("账号管理");
    }//GEN-LAST:event_jLabel_accountMouseExited

    private void jLabel_logOutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_logOutMouseEntered
        // TODO add your handling code here:
        jLabel_logOut.setText("<html><u><b>注销</b><u></html>");
    }//GEN-LAST:event_jLabel_logOutMouseEntered

    private void jLabel_logOutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_logOutMouseExited
        // TODO add your handling code here:
        jLabel_logOut.setText("注销");
    }//GEN-LAST:event_jLabel_logOutMouseExited

    private void jLabel_logOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_logOutMouseClicked
        // TODO add your handling code here:
        LoginUI T = new LoginUI();
        T.setLocationRelativeTo(null);
        T.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jLabel_logOutMouseClicked

    private void jButton_modifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_modifyActionPerformed
        // TODO add your handling code here:
        setEdit(true);
        buttonVisible(true);
    }//GEN-LAST:event_jButton_modifyActionPerformed

    private void jButton_backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_backActionPerformed
        // TODO add your handling code here:
        int result = JOptionPane.showConfirmDialog(this, "确定退出？未保存的内容将丢失！", "提示信息", JOptionPane.YES_NO_OPTION);
        if (result == 0) { //YES
            displayInfo(false);
        }
    }//GEN-LAST:event_jButton_backActionPerformed

    private void jButton_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_saveActionPerformed
        // TODO add your handling code here:
        try {
            saveData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存失败！\n" + e.getMessage(), "提示信息", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "保存成功！", "提示信息", JOptionPane.INFORMATION_MESSAGE);
        displayInfo(false);
    }//GEN-LAST:event_jButton_saveActionPerformed

    private void jTable_coursesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_coursesMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_jTable_coursesMouseClicked

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveButtonActionPerformed

    private void saveButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveButtonMouseClicked
        // TODO add your handling code here:

        boolean flag = gradeInputModel.commitGrades();
        if (flag == true) {
            courseInfoModel.update();
        }
    }//GEN-LAST:event_saveButtonMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_back;
    private javax.swing.JButton jButton_modify;
    private javax.swing.JButton jButton_save;
    private javax.swing.JComboBox<String> jComboBox_sex;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_IDnum;
    private javax.swing.JLabel jLabel_account;
    private javax.swing.JLabel jLabel_address;
    private javax.swing.JLabel jLabel_birthday;
    private javax.swing.JLabel jLabel_cell;
    private javax.swing.JLabel jLabel_depart;
    private javax.swing.JLabel jLabel_email;
    private javax.swing.JLabel jLabel_hello;
    private javax.swing.JLabel jLabel_hometown;
    private javax.swing.JLabel jLabel_id;
    private javax.swing.JLabel jLabel_logOut;
    private javax.swing.JLabel jLabel_name;
    private javax.swing.JLabel jLabel_title;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable_courses;
    private javax.swing.JTable jTable_scores;
    private javax.swing.JTextField jTextField_IDnum;
    private javax.swing.JTextField jTextField_address;
    private javax.swing.JTextField jTextField_birthday;
    private javax.swing.JTextField jTextField_cell;
    private javax.swing.JTextField jTextField_depart;
    private javax.swing.JTextField jTextField_email;
    private javax.swing.JTextField jTextField_hometown;
    private javax.swing.JTextField jTextField_id;
    private javax.swing.JTextField jTextField_name;
    private javax.swing.JTextField jTextField_sex;
    private javax.swing.JTextField jTextField_title;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
