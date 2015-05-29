package com.rasalhague.ereminisce.properties;

import java.util.HashMap;

public interface PropertiesParser
{
    HashMap<String, String> parseProperties(String regex);
}
