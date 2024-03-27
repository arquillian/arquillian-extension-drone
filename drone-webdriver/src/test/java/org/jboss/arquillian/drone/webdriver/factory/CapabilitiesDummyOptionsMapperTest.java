/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.webdriver.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

public class CapabilitiesDummyOptionsMapperTest {

    private static final Gson GSON = new Gson();

    @Test
    public void testParseDummyOptions() throws IOException {
        DummyBrowserOptions dummyBrowserOptions = new DummyBrowserOptions();
        DummyBrowserOptions expectedDummyBrowserOptions = prepareDummyBrowserOptions();
        Capabilities capabilities = prepareCapabilities(expectedDummyBrowserOptions);

        CapabilitiesOptionsMapper.mapCapabilities(dummyBrowserOptions, capabilities, "dummy");

        Assert.assertEquals(expectedDummyBrowserOptions, dummyBrowserOptions);
    }

    private DummyBrowserOptions prepareDummyBrowserOptions() {
        DummyBrowserOptions dummyBrowserOptions = new DummyBrowserOptions();
        dummyBrowserOptions.setStringParam("stringParam");
        dummyBrowserOptions.setLongParam("longParam".hashCode());
        dummyBrowserOptions.setDoubleParam("doubleParam".hashCode());
        dummyBrowserOptions.setIntParam("intParam".hashCode());
        dummyBrowserOptions.setIntegerParam("integerParam".hashCode());
        dummyBrowserOptions.setBooleanParam(false);
        dummyBrowserOptions.setFileParam(new File("fileParam"));

        ArrayList<String> listOfStrings = new ArrayList<String>();
        listOfStrings.add("--firstString=ListParam");
        listOfStrings.add("--secondStringListParam");
        dummyBrowserOptions.setListOfStringsParam(listOfStrings);

        ArrayList<File> listOfFiles = new ArrayList<File>();
        listOfFiles.add(new File("firstFileListParam"));
        listOfFiles.add(new File("secondFileListParam"));
        dummyBrowserOptions.setListOfFilesParam(listOfFiles);

        ArrayList<Long> listOfLongs = new ArrayList<Long>();
        listOfLongs.add(Long.valueOf("firstLongListParam".hashCode()));
        listOfLongs.add(Long.valueOf("secondLongListParam".hashCode()));
        dummyBrowserOptions.setListOfLongsParam(listOfLongs);

        String[] arrayOfStrings = new String[] {"--firstString=ArrayParam", "--secondStringArrayParam"};
        dummyBrowserOptions.setArrayOfStringsParam(arrayOfStrings);

        File[] arrayOfFiles = new File[] {new File("firstFileArrayParam"), new File("secondFileArrayParam")};
        dummyBrowserOptions.setArrayOfFilesParam(arrayOfFiles);

        Double[] arrayOfDoubles = new Double[] { Double.valueOf("firstDoubleArrayParam".hashCode()),
            Double.valueOf("secondDoubleArrayParam".hashCode())};
        dummyBrowserOptions.setArrayOfDoublesParam(arrayOfDoubles);

        dummyBrowserOptions.setMapOfMapOfStringsParam("mapOfStrings", getMapOfStrings());

        dummyBrowserOptions.setMapOfObjectParam("mapOfObjectsInString", getMapOfObjectsInString());
        dummyBrowserOptions.setMapOfObjectParam("mapOfStrings", getMapOfStrings());

        return dummyBrowserOptions;
    }

    private Map<String, String> getMapOfStrings() {
        Map<String, String> mapOfStrings = new HashMap<String, String>();
        mapOfStrings.put("firstKeyInMapOfString", "firstValueInMapOfString");
        mapOfStrings.put("secondKeyInMapOfString", "secondValueInMapOfString");
        return mapOfStrings;
    }

    private Map<String, String> getMapOfObjectsInString() {
        Map<String, String> mapOfObjectsInString = new HashMap<String, String>();
        mapOfObjectsInString.put("firstKeyInMapOfObject", "firstValueInMapOfString");
        mapOfObjectsInString.put("secondKeyInMapOfObject", "secondValueInMapOfString");
        return mapOfObjectsInString;
    }

    private Map<String, Map<String, String>> getMapOfMapOfObjectsInMapOfStrings() {
        Map<String, Map<String, String>> mapOfMapOfObjects = new HashMap<String, Map<String, String>>();
        mapOfMapOfObjects.put("mapOfObjectsInString", getMapOfObjectsInString());
        mapOfMapOfObjects.put("mapOfStrings", getMapOfStrings());
        return mapOfMapOfObjects;
    }

    private Capabilities prepareCapabilities(DummyBrowserOptions dummyBrowserOptions) {
        MutableCapabilities capabilities = new MutableCapabilities();

        capabilities
            .setCapability("dummyStringParam", dummyBrowserOptions.getStringParam());

        capabilities
            .setCapability("dummyLongParam", String.valueOf(dummyBrowserOptions.getLongParam()));

        capabilities
            .setCapability("dummyDoubleParam", String.valueOf(dummyBrowserOptions.getDoubleParam()));

        capabilities
            .setCapability("dummyIntParam", String.valueOf(dummyBrowserOptions.getIntParam()));

        capabilities
            .setCapability("dummyIntegerParam", String.valueOf(dummyBrowserOptions.getIntegerParam()));

        capabilities
            .setCapability("dummyBooleanParam", String.valueOf(dummyBrowserOptions.isBooleanParam()));

        capabilities
            .setCapability("dummyFileParam", dummyBrowserOptions.getFileParam().toString());

        capabilities
            .setCapability("dummyListOfStringsParam",
                getListOrArray(dummyBrowserOptions.getListOfStringsParam().toArray()));

        capabilities
            .setCapability("dummyListOfFilesParam",
                getListOrArray(dummyBrowserOptions.getListOfFilesParam().toArray()));

        capabilities
            .setCapability("dummyListOfLongsParam",
                getListOrArray(dummyBrowserOptions.getListOfLongsParam().toArray()));

        capabilities
            .setCapability("dummyArrayOfStringsParam", getListOrArray(dummyBrowserOptions.getArrayOfStringsParam()));

        capabilities
            .setCapability("dummyArrayOfFilesParam", getListOrArray(dummyBrowserOptions.getArrayOfFilesParam()));

        capabilities
            .setCapability("dummyArrayOfDoublesParam", getListOrArray(dummyBrowserOptions.getArrayOfDoublesParam()));

        capabilities
            .setCapability("dummyMapOfMapOfStringsParam",
                getJsonString(dummyBrowserOptions.getMapOfMapOfStringsParam()));

        capabilities
            .setCapability("dummyMapOfObjectParam", getJsonString(getMapOfMapOfObjectsInMapOfStrings()));

        return capabilities;
    }

    private String getListOrArray(Object[] array) {
        StringBuffer sb = new StringBuffer();
        for (Object o : array) {
            sb.append(o.toString()).append(" ");
        }
        return sb.toString().trim();
    }

    private String getJsonString(Map<String, Map<String, String>> mapOfStrings) {
        return GSON.toJson(mapOfStrings).toString();
    }

    private class DummyBrowserOptions {

        private String stringParam;
        private long longParam;
        private double doubleParam;
        private int intParam;
        private Integer integerParam;
        private boolean booleanParam;
        private File fileParam;

        private List<String> listOfStringsParam;
        private List<File> listOfFilesParam;
        private List<Long> listOfLongsParam;

        private String[] arrayOfStringsParam;
        private File[] arrayOfFilesParam;
        private Double[] arrayOfDoublesParam;

        private Map<String, Map<String, String>> mapOfMapOfStringsParam = new HashMap<String, Map<String, String>>();
        private Map<String, Object> mapOfObjectParam = new HashMap<String, Object>();

        public String getStringParam() {
            return stringParam;
        }

        public void setStringParam(String stringParam) {
            this.stringParam = stringParam;
        }

        public long getLongParam() {
            return longParam;
        }

        public void setLongParam(long longParam) {
            this.longParam = longParam;
        }

        public double getDoubleParam() {
            return doubleParam;
        }

        public void setDoubleParam(double doubleParam) {
            this.doubleParam = doubleParam;
        }

        public int getIntParam() {
            return intParam;
        }

        public void setIntParam(int intParam) {
            this.intParam = intParam;
        }

        public Integer getIntegerParam() {
            return integerParam;
        }

        public void setIntegerParam(Integer integerParam) {
            this.integerParam = integerParam;
        }

        public boolean isBooleanParam() {
            return booleanParam;
        }

        public void setBooleanParam(boolean booleanParam) {
            this.booleanParam = booleanParam;
        }

        public File getFileParam() {
            return fileParam;
        }

        public void setFileParam(File fileParam) {
            this.fileParam = fileParam;
        }

        public List<String> getListOfStringsParam() {
            return listOfStringsParam;
        }

        public void setListOfStringsParam(List<String> listOfStringsParam) {
            this.listOfStringsParam = listOfStringsParam;
        }

        public List<File> getListOfFilesParam() {
            return listOfFilesParam;
        }

        public void setListOfFilesParam(List<File> listOfFilesParam) {
            this.listOfFilesParam = listOfFilesParam;
        }

        public List<Long> getListOfLongsParam() {
            return listOfLongsParam;
        }

        public void setListOfLongsParam(List<Long> listOfLongsParam) {
            this.listOfLongsParam = listOfLongsParam;
        }

        public String[] getArrayOfStringsParam() {
            return arrayOfStringsParam;
        }

        public void setArrayOfStringsParam(String[] arrayOfStringsParam) {
            this.arrayOfStringsParam = arrayOfStringsParam;
        }

        public File[] getArrayOfFilesParam() {
            return arrayOfFilesParam;
        }

        public void setArrayOfFilesParam(File[] arrayOfFilesParam) {
            this.arrayOfFilesParam = arrayOfFilesParam;
        }

        public Double[] getArrayOfDoublesParam() {
            return arrayOfDoublesParam;
        }

        public void setArrayOfDoublesParam(Double[] arrayOfDoublesParam) {
            this.arrayOfDoublesParam = arrayOfDoublesParam;
        }

        public Map<String, Map<String, String>> getMapOfMapOfStringsParam() {
            return mapOfMapOfStringsParam;
        }

        public void setMapOfMapOfStringsParam(String paramName, Map<String, String> mapOfMapOfStringsParam) {
            this.mapOfMapOfStringsParam.put(paramName, mapOfMapOfStringsParam);
        }

        public Map<String, Object> getMapOfObjectParam() {
            return mapOfObjectParam;
        }

        public void setMapOfObjectParam(String paramName, Object mapOfObjectParam) {
            this.mapOfObjectParam.put(paramName, mapOfObjectParam);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DummyBrowserOptions that = (DummyBrowserOptions) o;

            if (longParam != that.longParam) {
                return false;
            }
            if (Double.compare(that.doubleParam, doubleParam) != 0) {
                return false;
            }
            if (intParam != that.intParam) {
                return false;
            }
            if (booleanParam != that.booleanParam) {
                return false;
            }
            if (stringParam != null ? !stringParam.equals(that.stringParam) : that.stringParam != null) {
                return false;
            }
            if (integerParam != null ? !integerParam.equals(that.integerParam) : that.integerParam != null) {
                return false;
            }
            if (fileParam != null ? !fileParam.equals(that.fileParam) : that.fileParam != null) {
                return false;
            }
            if (listOfStringsParam != null ? !listOfStringsParam.equals(that.listOfStringsParam) :
                that.listOfStringsParam != null) {
                return false;
            }
            if (listOfFilesParam != null ? !listOfFilesParam.equals(that.listOfFilesParam) :
                that.listOfFilesParam != null) {
                return false;
            }
            if (listOfLongsParam != null ? !listOfLongsParam.equals(that.listOfLongsParam) :
                that.listOfLongsParam != null) {
                return false;
            }
            if (!Arrays.equals(arrayOfStringsParam, that.arrayOfStringsParam)) {
                return false;
            }
            if (!Arrays.equals(arrayOfFilesParam, that.arrayOfFilesParam)) {
                return false;
            }
            if (!Arrays.equals(arrayOfDoublesParam, that.arrayOfDoublesParam)) {
                return false;
            }
            if (mapOfMapOfStringsParam != null ? !mapOfMapOfStringsParam.equals(that.mapOfMapOfStringsParam) :
                that.mapOfMapOfStringsParam != null) {
                return false;
            }
            if (mapOfObjectParam != null ? !mapOfObjectParam.equals(that.mapOfObjectParam) :
                that.mapOfObjectParam != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = stringParam != null ? stringParam.hashCode() : 0;
            result = 31 * result + (int) (longParam ^ (longParam >>> 32));
            temp = Double.doubleToLongBits(doubleParam);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + intParam;
            result = 31 * result + (integerParam != null ? integerParam.hashCode() : 0);
            result = 31 * result + (booleanParam ? 1 : 0);
            result = 31 * result + (fileParam != null ? fileParam.hashCode() : 0);
            result = 31 * result + (listOfStringsParam != null ? listOfStringsParam.hashCode() : 0);
            result = 31 * result + (listOfFilesParam != null ? listOfFilesParam.hashCode() : 0);
            result = 31 * result + (listOfLongsParam != null ? listOfLongsParam.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(arrayOfStringsParam);
            result = 31 * result + Arrays.hashCode(arrayOfFilesParam);
            result = 31 * result + Arrays.hashCode(arrayOfDoublesParam);
            result = 31 * result + (mapOfMapOfStringsParam != null ? mapOfMapOfStringsParam.hashCode() : 0);
            result = 31 * result + (mapOfObjectParam != null ? mapOfObjectParam.hashCode() : 0);
            return result;
        }
    }
}
