package com.rasalhague.ereminisce.properties;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Properties implements PropertiesNames
{
    private String                  propertiesLine;
    private String[]                propertiesArray;
    private HashMap<String, String> propertiesMap;

    public Properties(String[] propertiesArray)
    {
        this.propertiesArray = propertiesArray;
        this.propertiesLine = Arrays.toString(propertiesArray);

        this.propertiesMap = generatePropertiesMap(propertiesArray);
    }

    public HashMap<String, String> getPropertiesMap()
    {
        return propertiesMap;
    }

    private HashMap<String, String> generatePropertiesMap(String[] properties)
    {
        HashMap<String, String> nameToPropertyValue = new HashMap<>();

        Field[] fields = PropertiesNames.class.getFields();
        try
        {
            for (Field field : fields)
            {
                String propertyName = (String) field.get(null);

                Pattern pattern = Pattern.compile(propertyName + "=(?<Result>.*)");
                for (String property : properties)
                {
                    Matcher matcher = pattern.matcher(property);
                    while (matcher.find())
                    {
                        String result = matcher.group("Result");
                        nameToPropertyValue.put(propertyName, result);
                    }
                }
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return nameToPropertyValue;
    }
}
