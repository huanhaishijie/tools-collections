package com.yuezm.project.common;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * word 模版工具
 *
 * @author Dulihong
 * @since 2022-11-02
 */
@SuppressWarnings("unchecked")
public class WordTemplateUtil {

    // 改变中文字体设置这个
    public static String fontName = "仿宋";

    // 改变数字或者英文字体需要设置这个
    public static String ascii = "Times New Roman";
    /**
     * 根据模板生成新word文档
     * 判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
     *
     * @param in           模版文档输入流
     * @param outputStream 新文档存放地址
     * @param textMap      需要替换的信息集合，通过${key}的方式替换
     * @param tableList    需要插入的表格信息集合 模版表格至少有两行 不替换表头
     *                     可变参数顺序根据模版表格中插入表格的顺序
     * @return 成功返回true, 失败返回false
     */
    public static boolean changWord(InputStream in, OutputStream outputStream,
                                    Map<String, String> textMap, int landSize, List<String[]>... tableList) {

        //模板转换默认成功
        boolean changeFlag = true;
        try {
            //获取docx解析对象
            XWPFDocument document = new XWPFDocument(in);
            //解析替换文本段落对象
            changeText(document, textMap);
            //解析替换表格对象
            changeTable(document, textMap, landSize, tableList);
            //生成新的word
            document.write(outputStream);
            closeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
            changeFlag = false;
        }
        return changeFlag;
    }

    /**
     * 替换段落文本
     *
     * @param document docx解析对象
     * @param textMap  需要替换的信息集合
     */
    public static void changeText(XWPFDocument document, Map<String, String> textMap) {
        //获取段落集合
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        for (XWPFParagraph paragraph : paragraphs) {
            //判断此段落时候需要进行替换
            String text = paragraph.getText();
            if (checkText(text)) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    //替换模板原来位置
                    run.setText(changeValue(run.toString(), textMap), 0);
                    run.setFontFamily(fontName);
                }
            }
        }

    }

    /**
     * 替换表格对象方法
     *
     * @param document  docx解析对象
     * @param textMap   需要替换的信息集合
     * @param tableList 需要插入的表格信息集合
     */
    public static void changeTable(XWPFDocument document, Map<String, String> textMap, Integer landSize,
                                   List<String[]>... tableList) {
        //获取表格对象集合
        List<XWPFTable> tables = document.getTables();
        for (int i = 0, j = 0; i < tables.size(); i++) {
            //只处理行数大于等于2的表格，且不循环表头
            XWPFTable table = tables.get(i);
            //判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
            if (checkText(table.getText())) {
                List<XWPFTableRow> rows = table.getRows();
                //遍历表格,并替换模板
                eachTable(rows, textMap);
            } else {
                if (j >= tableList.length) {
                    return;
                }
                insertTableReplaceHeader(table, tableList[j]);

                // 根据地块数量合并
                int currentRow = 0;

                // 地块基本信息合并
                mergeCellsVertically(table, 0, 0, landSize * 5);

                for (int k = 0; k < landSize; k++) {
                    // 坐落合并
                    mergeCellsHorizontal(table, currentRow + 4, 2, 4);
                    currentRow += 5;
                }
                // 地块转让信息合并
                mergeCellsVertically(table, 0, currentRow, currentRow + 5);


                currentRow += 6;
                // 地块名称合并
                mergeCellsHorizontal(table, currentRow - 6, 2, 4);
                // 说明合并
                mergeCellsHorizontal(table, currentRow - 1, 2, 4);


                // 地块审核材料合并
                mergeCellsVertically(table, 0, currentRow, currentRow + 2);

                currentRow += 3;

                // 地块宣传材料合并
                mergeCellsVertically(table, 0, currentRow, currentRow);
                j++;
            }
        }

    }


    /**
     * 遍历表格
     *
     * @param rows    表格行对象
     * @param textMap 需要替换的信息集合
     */
    public static void eachTable(List<XWPFTableRow> rows, Map<String, String> textMap) {
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            for (XWPFTableCell cell : cells) {
                //判断单元格是否需要替换
                if (checkText(cell.getText())) {
                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                    for (XWPFParagraph paragraph : paragraphs) {
                        List<XWPFRun> runs = paragraph.getRuns();
                        for (XWPFRun run : runs) {
                            run.setText(changeValue(run.toString(), textMap), 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * 为表格插入数据，行数不够添加新行
     *
     * @param table     需要插入数据的表格
     * @param tableList 插入数据集合
     */
    public static void insertTable(XWPFTable table, List<String[]> tableList) {
        if (tableList == null) {
            return;
        }
        //创建行,根据需要插入的数据添加新行，不处理表头
        for (int i = 1; i < tableList.size(); i++) {
            XWPFTableRow row = table.createRow();
        }
        //遍历表格插入数据
        List<XWPFTableRow> rows = table.getRows();
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                cell.setText(tableList.get(i - 1)[j]);
            }
        }
    }

    public static void insertTableReplaceHeader(XWPFTable table, List<String[]> tableList) {
        if (tableList == null) {
            return;
        }
        //创建行,根据需要插入的数据添加新行，不处理表头
        for (int i = 0; i < tableList.size() - 1; i++) {
            XWPFTableRow row = table.createRow();
        }
        //遍历表格插入数据
        XWPFTableRow sourceRow = table.getRow(0);
        List<XWPFTableCell> sourceCells = sourceRow.getTableCells();

        List<XWPFTableRow> rows = table.getRows();
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            newRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell sourceCell = sourceCells.get(j);
                XWPFTableCell cell = cells.get(j);

                List<XWPFParagraph> paragraphs = cell.getParagraphs();
                for (int x = 0; x < paragraphs.size(); x++) {
                    XWPFParagraph xwpfParagraph = paragraphs.get(x);
                    XWPFRun run = xwpfParagraph.createRun();
                    run.setText(tableList.get(i)[j]);
                    CTFonts font = run.getCTR().addNewRPr().addNewRFonts();
                    //中文
                    font.setEastAsia(fontName);
                    // ASCII
                    font.setAscii(ascii);
                }

                CTTc cttc = cell.getCTTc();
                CTTcPr tcPr = sourceCell.getCTTc().getTcPr();
                cttc.setTcPr(tcPr);
            }
        }
    }

    /**
     * word单元格行合并
     *
     * @param table 表格
     * @param col   合并行所在列
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int startRow, int endRow) {
        for (int i = startRow; i <= endRow; i++) {
            XWPFTableCell cell = table.getRow(i).getCell(col);
            if (i == startRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * word单元格列合并
     *
     * @param table     表格
     * @param row       合并列所在行
     * @param startCell 开始列
     * @param endCell   结束列
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int startCell, int endCell) {
        for (int i = startCell; i <= endCell; i++) {
            XWPFTableCell cell = table.getRow(row).getCell(i);
            if (i == startCell) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    public static void union(XWPFTable table) {
        //合并列：如果表格的第一列的上下两个单元格相同，进行合并。
        for (int row = 0; row < table.getRows().size(); row++) {
            if (row == table.getRows().size() - 1) {
                continue; //最后一行跨过去
            }
            XWPFTableCell cell0 = table.getRow(row).getCell(0);
            XWPFTableCell cell1 = table.getRow(row + 1).getCell(0);
            String test0 = cell0.getText();
            String test1 = cell1.getText();
            if (test0.equals(test1)) {
                CTVMerge vmerge = CTVMerge.Factory.newInstance();
                vmerge.setVal(STMerge.RESTART);
                cell0.getCTTc().getTcPr().setVMerge(vmerge);
                cell1.getCTTc().getTcPr().setVMerge(vmerge);

                // Second Row cell will be merged ,设置合并的结束点
                CTVMerge vmerge1 = CTVMerge.Factory.newInstance();
                vmerge1.setVal(STMerge.CONTINUE);
                cell0.getCTTc().getTcPr().setVMerge(vmerge1);
                cell1.getCTTc().getTcPr().setVMerge(vmerge1);
            }
        }
        //合并行：如果一行中最后面的单元格全是空行，则合并它们
        for (XWPFTableRow currentRow : table.getRows()) {
            int cellHasText = 0;//记录从第几个单元格以后开始为空的
            for (int i = currentRow.getTableCells().size() - 1; i >= 0; i--) {
                if (!currentRow.getCell(i).getText().equals("")) {
                    cellHasText = i;
                    break;
                }
            }
            if (cellHasText > 0) {//开始合并
                for (int i = cellHasText; i < currentRow.getTableCells().size(); i++) {
//                          System.out.println("开始合并行了");
                    if (i == cellHasText) {
                        currentRow.getCell(i).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
                    } else {
                        currentRow.getCell(i).getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
                    }
                }
            }
        }
    }

    /**
     * 判断文本中时候包含$
     *
     * @param text 文本
     * @return 包含返回true, 不包含返回false
     */
    public static boolean checkText(String text) {
        return text.contains("$");
    }

    /**
     * 匹配传入信息集合与模板
     *
     * @param value   模板需要替换的区域
     * @param textMap 传入信息集合
     * @return 模板需要替换区域信息集合对应值
     */
    public static String changeValue(String value, Map<String, String> textMap) {
        Set<Map.Entry<String, String>> textSets = textMap.entrySet();
        for (Map.Entry<String, String> textSet : textSets) {
            if (null == value) {
                value = "";
            }
            //匹配模板与替换值 格式${key}
            String key = "${" + textSet.getKey() + "}";
            if (value.contains(key) && null != textSet.getValue()) {
                value = textSet.getValue();
            }
        }
        //模板未匹配到区域替换为空
        if (checkText(value)) {
            value = "";
        }
        return value;
    }

    /**
     * 关闭输入流
     */
    private static void closeStream(InputStream is) {

        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
