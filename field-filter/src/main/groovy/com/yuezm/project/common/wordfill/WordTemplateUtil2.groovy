package com.yuezm.project.common.wordfill

import com.deepoove.poi.xwpf.NiceXWPFDocument
import org.apache.poi.xwpf.usermodel.*

import java.lang.reflect.Field

class WordTemplateUtil2 {


    <P> XWPFDocument fill(String templatePath, P params) {
        if(!params){
            return null
        }
        def clazz = params.class
        try {
            XWPFDocument doc = new XWPFDocument(this.getClass().classLoader.getResourceAsStream(templatePath))
            Map<String, Object> mapOfValIsList = [:]
            Map<String, Object> map = [:]
            clazz.getFields().each {
                if(it.getName().contains("\$") || it.getName().equals("metaClass")) {
                    return
                }

                FillList annotation = it.getAnnotation(FillList.class)
                if(!annotation){
                    try {
                        Class<?> aopProxyUtilsClass = Class.forName("org.springframework.aop.framework.AopProxyUtils")
                        def t = (Class)aopProxyUtilsClass.getMethod("ultimateTargetClass", Object.class).invoke(null, params)
                        def field1 = t.getField(it.name)
                        if(!field1){
                            field1 = t.getDeclaredField(it.name)
                            field1.accessible = true
                        }
                        annotation = it.getAnnotation(FillList.class)
                    }catch (Exception e) {
                    }
                }
                if(annotation){
                    mapOfValIsList[it.name] = it.get(params)
                }
                map[it.name] = it.get(params)
            }

            clazz.declaredFields.each {
                if(it.getName().contains("\$") || it.getName().equals("metaClass")) {
                    return
                }
                it.accessible = true
                FillList annotation = it.getAnnotation(FillList.class)
                if(!annotation){
                    try {
                        Class<?> aopProxyUtilsClass = Class.forName("org.springframework.aop.framework.AopProxyUtils")
                        def t = (Class)aopProxyUtilsClass.getMethod("ultimateTargetClass", Object.class).invoke(null, params)
                        def field1 = t.getField(it.name)
                        if(!field1){
                            field1 = t.getDeclaredField(it.name)
                            field1.accessible = true
                        }
                        annotation = it.getAnnotation(FillList.class)
                    }catch (Exception e) {
                    }
                }
                if(annotation){
                    mapOfValIsList[it.name] = it.get(params)
                }
                map[it.name] = it.get(params)
            }


            doc.paragraphs.each {
                it.runs.each { run ->
                    String text = run.getText(0)
                    if(text && text.length() > 0 && text.contains("\${")){
                        def ps = extractParams(text)
                        ps.each {p ->
                            if(map.containsKey(p) && map[p] != null){
                                if(map[p] instanceof List){
                                    return
                                }
                                def val = map[p]?.toString()
                                if(val != null){
                                    text = text.replace("\${$p}", val)
                                    run.setText(text, 0)
                                }
                            }
                        }
                    }
                }
            }

            if(mapOfValIsList.size() > 0){
                mapOfValIsList.each { k,v ->

                    def field = null;
                    try {
                        field = clazz.getField(k)
                    }catch (Exception e) {

                    }
                    if(!field){
                        try {
                            field = clazz.getDeclaredField(k)
                            field.accessible = true
                        }catch (Exception e) {

                        }

                    }
                    FillList annotation = field.getAnnotation(FillList.class)
                    if(!annotation){
                        try {
                            Class<?> aopProxyUtilsClass = Class.forName("org.springframework.aop.framework.AopProxyUtils")
                            def t = (Class)aopProxyUtilsClass.getMethod("ultimateTargetClass", Object.class).invoke(null, params)
                            def field1 = t.getField(k)
                            if(!field1){
                                field1 = t.getDeclaredField(k)
                                field1.accessible = true
                            }
                            annotation = field.getAnnotation(FillList.class)
                        }catch (Exception e) {
                            e.printStackTrace()
                        }
                    }
                    if(!annotation){
                        return
                    }
                    def index = annotation.index()
                    XWPFTable table = doc.getTables().get(index)
                    if(!table){
                        return
                    }

                    if(annotation.type().equals("irregular") && !(v instanceof List)) {

                        def rows = table.getRows()
                        for(i in 0..< rows.size()) {
                            XWPFTableRow row = rows.get(i)
                            for(j in 0..< row.getTableCells().size()) {
                                XWPFTableCell cell = row.getCell(j)
                                cell.paragraphs.each {
                                    it.runs.each { run ->
                                        String t = run.getText(0)
                                        if(t && t.length() > 0 && t.contains("\${")){
                                            def ps = extractParams(t)
                                            ps.each {p ->

                                                def v1 = getVal(v, p as String)
                                                if(v1 != null){
                                                    t = t.replace("\${$p}", v1?.toString())
                                                    run.setText(t, 0)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        def templateRow = table.getRow(1)
                        (v as List).each { val ->
                            if(!val){
                                return
                            }
                            XWPFTableRow row = table.createRow()
                            for (i in 0..< templateRow.tableCells.size()) {
                                XWPFTableCell templateCell = templateRow.getCell(i)
                                XWPFTableCell cell = row.getCell(i)
                                if(!cell){
                                    cell = row.createCell()
                                }
                                copyCellStyleWithoutText(templateCell, cell)
                                def text = templateCell.getText()
                                def key = extractParams(text)?[0] as String
                                def val1 = getVal(val, key)
                                if(val1 != null){
                                    cell.setText(text.replace("\${$key}", val1?.toString()))
                                }

                            }
                        }
                        table.removeRow(1)

                    }
                }
            }
            return doc
        }catch (Exception e) {
            e.printStackTrace()
        }
        return null
    }

    def extractParams(String input) {
        def pattern = /\$\{(\w+)\}/
        def matcher = (input =~ pattern)
        def params = []

        while (matcher.find()) {
            params << matcher.group(1) // 提取参数名
        }

        return params
    }

    def getVal(Object target, String fieldName ) {
        if(!target){
            return null
        }
        def clazz = target.getClass()
        try{
            if(target?."$fieldName"){
                return target?."$fieldName"
            }
        }catch (Exception e){

        }
        try {
            def field = clazz.getField(fieldName)
            if(!field){
                field = clazz.getDeclaredField(fieldName)
                field.accessible = true
            }
            return field.get(target)
        }catch (Exception e){

        }


    }


    private void copyCellStyleWithoutText(XWPFTableCell sourceCell, XWPFTableCell targetCell){
        targetCell.getParagraphs().each {paragraph ->
            XWPFParagraph sourceParagraph = sourceCell.getParagraphs().get(0)
            paragraph.setAlignment(sourceParagraph.getAlignment())
            paragraph.setVerticalAlignment(sourceParagraph.getVerticalAlignment())

            if (!sourceParagraph.getRuns().isEmpty()) {
                XWPFRun sourceRun = sourceParagraph.getRuns().get(0)
                XWPFRun targetRun = paragraph.createRun()

                targetRun.setBold(sourceRun.isBold())
                targetRun.setItalic(sourceRun.isItalic())
                targetRun.setFontFamily(sourceRun.getFontFamily())
                targetRun.setFontSize(sourceRun.getFontSize())
            }
        }

        // 复制单元格背景色等样式
        targetCell.setColor(sourceCell.getColor())
        targetCell.setVerticalAlignment(sourceCell.getVerticalAlignment())
    }


    def mergeDocuments(List<XWPFDocument> documents) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        documents[0].write(byteArrayOutputStream)
        byte[] bytes = byteArrayOutputStream.toByteArray()
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)
        NiceXWPFDocument mainDoc = new NiceXWPFDocument(inputStream)
        for (int i = 1; i < documents.size(); i++) {
            byteArrayOutputStream.flush()
            byteArrayOutputStream.reset()
            documents[i].write(byteArrayOutputStream)
            bytes = byteArrayOutputStream.toByteArray()
            inputStream.reset()
            inputStream = new ByteArrayInputStream(bytes)
            NiceXWPFDocument doc = new NiceXWPFDocument(inputStream)
            mainDoc = mainDoc.merge(doc)
        }
        try {
            byteArrayOutputStream.flush()
            byteArrayOutputStream.close()
            inputStream.close()
        }catch (Exception e){
            e.printStackTrace()
        }
        return mainDoc
    }

//    static void main(String[] args) {
//        def data = new FillData(apiName: "test",
//                basicInfo: [applications:"cccccc"],
//                url: "http://www.baidu.com",
//                list1: [["headerKey": "headerKey1", "desc": "desc1"]],
//                list2: [["paramName": "1", "b": "paramType", "requried": true, "desc": 1111]]
//        )
//        def data2 = new FillData(apiName: "test", basicInfo: [applications:"cccccc"], url: "http://www.baidu.com", list1: [["headerKey": "headerKey1", "desc": "desc1"]], list2: [["paramName": "1", "b": "paramType", "requried": true, "desc": 1111]])
//        def file = new File("C:\\Users\\SKZZ\\Desktop\\test1.docx")
//        if(!file.exists()){
//            file.createNewFile()
//        }
//
//        def fos = new FileOutputStream("C:\\Users\\SKZZ\\Desktop\\test1.docx")
//        WordTemplateUtil2 wordTemplateUtil = new WordTemplateUtil2()
//        def fill = wordTemplateUtil.fill("apiTemplate.docx", data)
//        def fill1 = wordTemplateUtil.fill("apiTemplate.docx", data2)
//        def merged = wordTemplateUtil.mergeDocuments([fill, fill1])
//        merged.write(fos)
//        merged.close()
//        fos.flush()
//        fos.close()
//    }
}
//class FillData{
//
//    def apiName
//
//    def url
//
//    @FillList(index = 0, type = "irregular")
//    def basicInfo
//
//    @FillList(index = 1)
//    def list1
//
//    @FillList(index = 2)
//    def list2
//
//
//
//
//}