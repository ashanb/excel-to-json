package com.bfu.family;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ExcelToJson {
    final static LinkedList<String> spouseT = new LinkedList<>();
    final static LinkedList<String> foundingParentsList = new LinkedList<>();

    public static void main(String[] args) throws Exception {
        try {
            // Bakmeedeniya Root
            final JsTreeMember jsTreeRoot =
                    new JsTreeMember("male2", "Abayalankara Herath Mudalige Charles Vincent Prera Bakmeedeniya", "Grate-Grate-GrandFather"); // ~ out

            jsTreeRoot.addChild(new JsTreeMember("Grate-Grate-GrandMother", "Ranathunga Mudiyanselage Muthumanike", "Grate-Grate-GrandMother")); // ~ out
            spouseT.add(".H");
            spouseT.add(".W");
            spouseT.add(".(D)");

            LinkedHashMap<String, Member> subTreeMap;
            LinkedHashMap<String, JsTreeMember> jsTreeSubTreeMap;

            String filePath =
                    args != null && args.length > 0 && args[0] != null? args[0] :"C:\\Users\\Ashan\\Downloads\\bfu\\FamilyTreeInfo-2022.03.13.zip";

            Path source = Paths.get(filePath);
            final String outputJsonPath = (args != null && args.length > 1 && args[1] != null) ? args[1] : "D:\\my-projects\\github\\family\\dist\\node\\data.json";

            if (filePath.endsWith("zip")) {
                try {
                    Path target = Paths.get(filePath.split(".zip")[0]);
                    unzipFolder(source, target);
                    System.out.println("File Unzip Completed.");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Read Excel Files Started.");

            File folder = new File(filePath.split(".zip")[0]);
            File[] listOfFiles = folder.listFiles();
            assert listOfFiles != null;
            List<File> files = Arrays.asList(listOfFiles);

            files.sort(new Comparator<>() {
                private final Comparator<String> NATURAL_SORT = new WindowsExplorerComparator();

                @Override
                public int compare(File o1, File o2) {
                    return NATURAL_SORT.compare(o1.getName(), o2.getName());
                }
            });

            int memberCounter = 2;
            for (File file : files) {
                subTreeMap = new LinkedHashMap<>();
                jsTreeSubTreeMap = new LinkedHashMap<>();

                if (file.isFile() && file.getName().endsWith(".xlsx")) {
                    System.out.println("Reading: " + file.getAbsolutePath());

                    try {
                        // Pass Excel to Java Flat Objects
                        String foundingParentName = readFromExcel(file.getAbsolutePath(), subTreeMap, jsTreeSubTreeMap);
                        // Interconnect Java Objects in to Tree.
                        convertToTreeNodes(foundingParentName, subTreeMap, jsTreeSubTreeMap);
                        // logs
                        jsTreeSubTreeMap
                                .get(foundingParentName)
                                .setTitle(jsTreeSubTreeMap.get(foundingParentName)
                                        .getTitle() + ", Registered Members : " + (jsTreeSubTreeMap.size() - 1));

                        // add to tree
                        jsTreeRoot.getChildren().get(0).addChild(jsTreeSubTreeMap.get(foundingParentName));
                        memberCounter = memberCounter + jsTreeSubTreeMap.size();
                    } catch (Exception e) {
                        //System.out.println("========= Error occurred!, Continue!! =========" + e);
                        throw e;
                    }
                }
            }
            jsTreeRoot.setTitle(jsTreeRoot.getTitle() + ", Registered Members : " + (memberCounter - 1));
            // Generate Tree Json from Java Tree.
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(outputJsonPath), jsTreeRoot);

            System.out.println();
            System.out.println("================= Conversion Summary =================");
            System.out.println();
            System.out.print("Succeeded Groups: [ ");
            foundingParentsList.forEach(m -> System.out.print(m + " "));
            System.out.print("]");
            System.out.println();

            System.out.println("Total Members: " + memberCounter);

            Path fileName = Path.of(outputJsonPath);

            String[] temp = Files.readString(fileName).split("\"id\":");

            System.out.println("Tree Node Count: " + (temp.length - 1));

            System.out.println("Json Conversion Completed, see file at: " + outputJsonPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unzipFolder(Path source, Path target) throws IOException {

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {

            // list files in zip
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {

                boolean isDirectory = false;
                if (zipEntry.getName().endsWith(File.separator)) {
                    isDirectory = true;
                }

                Path newPath = zipSlipProtect(zipEntry, target);

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }
                    // copy files, nio
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }

                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();

        }

    }

    // protect zip slip attack
    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
            throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }


    private static void convertToTreeNodes(String foundingParentName, LinkedHashMap<String, Member> subTreeMap, LinkedHashMap<String, JsTreeMember> jsTreeSubTreeMap) {
        for (Map.Entry<String, Member> tempMemberEntry : subTreeMap.entrySet()) {
            if ((tempMemberEntry.getKey().length() == 1)
                    || !(spouseT.contains(tempMemberEntry.getKey().substring(tempMemberEntry.getKey().lastIndexOf("."))))) {
                // Ex: J.1.1
                for (String s : spouseT) {
                    String spCode = tempMemberEntry.getKey() + s;
                    if (isMarried(subTreeMap, spCode)) {
                        // add spouse as a child
                        tempMemberEntry.getValue().addChild(subTreeMap.get(spCode));
                        jsTreeSubTreeMap.get(tempMemberEntry.getKey()).addChild(jsTreeSubTreeMap.get(spCode));
                    }
                }
            } else {
                // Ex: J.1.1.W
                // remove spouseT
                String rootParent = tempMemberEntry.getKey().substring(0, tempMemberEntry.getKey().lastIndexOf("."));
                // check and add children
                tempMemberEntry.getValue().addChildren(getChildren(subTreeMap, rootParent, tempMemberEntry.getKey()));

                jsTreeSubTreeMap.get(tempMemberEntry.getKey()).setChildren(getJsChildren(subTreeMap, jsTreeSubTreeMap, rootParent, tempMemberEntry.getKey()));
            }
        }
        System.out.println("Group: " + foundingParentName + ", InMemory Java Objects connected as Tree Nodes.");
    }

    private static String readFromExcel(
            String path,
            LinkedHashMap<String, Member> subTreeMap,
            LinkedHashMap<String, JsTreeMember> jsTreeSubTreeMap) throws Exception {

        String foundingParentName = null;
        FileInputStream excelFile = new FileInputStream(path);
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet datatypeSheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = datatypeSheet.iterator();
        int rowCounter = 0;

        while (iterator.hasNext()) {
            Member tempMember;
            JsTreeMember jsTreeTempMember;
            Row currentRow = iterator.next();
            if (rowCounter != 0) {
                String tempCode = readFromCell(currentRow.getCell(0));
                String tempNameEnglish = readFromCell(currentRow.getCell(1));
                String tempNameSinhala = readFromCell(currentRow.getCell(2));
                String tempGender = readFromCell(currentRow.getCell(3));
                String tempCountry = readFromCell(currentRow.getCell(4));
                String tempCity = readFromCell(currentRow.getCell(5));
                String tempCitizenship = readFromCell(currentRow.getCell(6));
                String tempTelephone = readFromCell(currentRow.getCell(7));
                String tempEmail = readFromCell(currentRow.getCell(8));
                String tempProfession = readFromCell(currentRow.getCell(9));
                String tempFather = readFromCell(currentRow.getCell(10));
                String tempMother = readFromCell(currentRow.getCell(11));

                if (tempCode != null) {
                    // validating code

                    if (tempCode.endsWith(".")) {
                        throw new IllegalArgumentException("Wrong Code for Member:  " + tempCode);
                    }
                    if (!tempCode.matches("^[a-zA-Z0-9.()]*$")) {
                        throw new IllegalArgumentException("Wrong Code for Member: " + tempCode);
                    }

                    if (tempFather != null) {
                        if (tempFather.endsWith(".")) {
                            throw new IllegalArgumentException("Wrong Code for Father: " + tempCode);
                        }
                        if (!tempFather.matches("^[a-zA-Z0-9.()]*$")) {
                            throw new IllegalArgumentException("Wrong Code for Father: " + tempCode);
                        }
                    }

                    if (tempMother != null) {
                        if (tempMother.endsWith(".")) {
                            throw new IllegalArgumentException("Wrong Code for Mother: " + tempCode);
                        }
                        if (!tempMother.matches("^[a-zA-Z0-9.()]*$")) {
                            throw new IllegalArgumentException("Wrong Code for Mother: " + tempCode);
                        }
                    }
                    if (tempCode.length() > 1) {
                        if (!(spouseT.contains(tempCode.substring(tempCode.lastIndexOf("."))))) {
                            if (tempMother == null) {
                                throw new IllegalArgumentException("Must Contain Mother Information for : " + tempCode);
                            }
                            if (tempFather == null) {
                                throw new IllegalArgumentException("Must Contain Father Information for :  " + tempCode);
                            }

                        }
                    }

                    tempMember =
                            new Member(
                                    tempCode,
                                    tempNameEnglish,
                                    tempNameSinhala,
                                    tempGender,
                                    tempCountry,
                                    tempCity,
                                    tempCitizenship,
                                    tempTelephone,
                                    tempEmail,
                                    tempProfession,
                                    tempFather,
                                    tempMother);

                    System.out.println(tempCode);

                    jsTreeTempMember =
                            new JsTreeMember(
                                    tempGender.equals("M") ? "male2" : "female2",
                                    fullName(tempNameEnglish, tempNameSinhala),
                                    generateTitle(tempCode, tempProfession, tempCountry, tempCitizenship)); // ~ out

                    subTreeMap.put(tempCode, tempMember);
                    jsTreeSubTreeMap.put(tempCode, jsTreeTempMember); // ~ out

                    if (rowCounter == 1) { // get subtree root.
                        foundingParentsList.add(tempCode);
                        foundingParentName = tempCode;
                    }
                }
            }
            rowCounter++;
        }
        System.out.println("Group: " + foundingParentName + ", Excel Converted to InMemory Java Objects completed!");
        return foundingParentName;
    }

    private static String fullName(String tempNameEnglish, String tempNameSinhala) {
        if (tempNameSinhala != null) {
            return tempNameEnglish + "(" + tempNameSinhala + ")";
        } else {
            return tempNameEnglish;
        }
    }

    private static String generateTitle(
            String tempCode, String tempProfession, String tempCountry, String tempCitizenship) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (tempCode != null) {
            stringBuilder.append(tempCode);
        }
        if (tempProfession != null) {
            stringBuilder.append(", ").append(tempProfession);
        }
        if (tempCountry != null) {
            stringBuilder.append(", ").append(tempCountry);
        }
        if (tempCitizenship != null) {
            stringBuilder.append(", ").append(tempCitizenship);
        }
        return stringBuilder.toString();
    }

    private static boolean isMarried(final LinkedHashMap<String, Member> subTreeMap, String SpCode) {
        try {
            return subTreeMap.get(SpCode) != null;
        } catch (Exception exception) {
            // do  nothing
        }
        return false;
    }

    private static LinkedHashMap<String, Member> getChildren(
            final LinkedHashMap<String, Member> subTreeMap, String rootParentCode, String connectedParentCode) {

        final LinkedHashMap<String, Member> tempChildren = new LinkedHashMap<>();
        try {
            // loop and check for children
            for (final String tempCode : subTreeMap.keySet()) {
                if (dotOccCount(tempCode) == dotOccCount(rootParentCode) + 1) {
                    // ignore adding spouseT
                    if (!(spouseT.contains(tempCode.substring(tempCode.lastIndexOf("."))))) {
                        if (tempCode.startsWith(rootParentCode)) {
                            String rootParentGender = subTreeMap.get(rootParentCode).getGender();
                            String getSpouseType = rootParentGender.equals("M") ? "Mother" : "Father";

                            if (getSpouseType.equals("Mother")) {
                                if (subTreeMap.get(tempCode).getMother().equals(connectedParentCode)) {
                                    tempChildren.put(tempCode, subTreeMap.get(tempCode));
                                }
                            } else {
                                if (subTreeMap.get(tempCode).getFather().equals(connectedParentCode)) {
                                    tempChildren.put(tempCode, subTreeMap.get(tempCode));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            // do  nothing
        }
        return tempChildren;
    }

    private static LinkedList<JsTreeMember> getJsChildren(
            final LinkedHashMap<String, Member> subTreeMap,
            final LinkedHashMap<String, JsTreeMember> jsTreeSubTreeMap,
            final String rootParentCode,
            final String connectedParentCode) {

        final LinkedList<JsTreeMember> tempChildren = new LinkedList<>();
        try {
            // loop and check for children
            for (final String tempCode : subTreeMap.keySet()) {
                if (dotOccCount(tempCode) == dotOccCount(rootParentCode) + 1) {
                    // ignore adding spouseT
                    if (!(spouseT.contains(tempCode.substring(tempCode.lastIndexOf("."))))) {
                        if (tempCode.startsWith(rootParentCode)) {
                            String rootParentGender = subTreeMap.get(rootParentCode).getGender();
                            String getSpouseType = rootParentGender.equals("M") ? "Mother" : "Father";

                            if (getSpouseType.equals("Mother")) {
                                if (subTreeMap.get(tempCode).getMother().equals(connectedParentCode)) {
                                    tempChildren.add(jsTreeSubTreeMap.get(tempCode));
                                }
                            } else {
                                if (subTreeMap.get(tempCode).getFather().equals(connectedParentCode)) {
                                    tempChildren.add(jsTreeSubTreeMap.get(tempCode));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception exception) {
            // do  nothing
        }
        return tempChildren;
    }

    private static int dotOccCount(String someString) {
        char someChar = '.';
        int count = 0;

        for (int i = 0; i < someString.length(); i++) {
            if (someString.charAt(i) == someChar) {
                count++;
            }
        }
        return count;
    }

    private static String readFromCell(Cell currentCell) {
        if (currentCell != null) {
            if (currentCell.getCellTypeEnum() == CellType.STRING) {
                return currentCell.getStringCellValue() + "";
            } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                return currentCell.getNumericCellValue() + "";
            }
        }
        return null;
    }
}

class WindowsExplorerComparator implements Comparator<String> {

    private static final Pattern splitPattern = Pattern.compile("\\d+|\\.|\\s");

    @Override
    public int compare(String str1, String str2) {
        Iterator<String> i1 = splitStringPreserveDelimiter(str1).iterator();
        Iterator<String> i2 = splitStringPreserveDelimiter(str2).iterator();
        while (true) {
            //Til here all is equal.
            if (!i1.hasNext() && !i2.hasNext()) {
                return 0;
            }
            //first has no more parts -> comes first
            if (!i1.hasNext() && i2.hasNext()) {
                return -1;
            }
            //first has more parts than i2 -> comes after
            if (i1.hasNext() && !i2.hasNext()) {
                return 1;
            }

            String data1 = i1.next();
            String data2 = i2.next();
            int result;
            try {
                //If both datas are numbers, then compare numbers
                result = Long.compare(Long.valueOf(data1), Long.valueOf(data2));
                //If numbers are equal than longer comes first
                if (result == 0) {
                    result = -Integer.compare(data1.length(), data2.length());
                }
            } catch (NumberFormatException ex) {
                //compare text case insensitive
                result = data1.compareToIgnoreCase(data2);
            }

            if (result != 0) {
                return result;
            }
        }
    }

    private List<String> splitStringPreserveDelimiter(String str) {
        Matcher matcher = splitPattern.matcher(str);
        List<String> list = new ArrayList<String>();
        int pos = 0;
        while (matcher.find()) {
            list.add(str.substring(pos, matcher.start()));
            list.add(matcher.group());
            pos = matcher.end();
        }
        list.add(str.substring(pos));
        return list;
    }
}
