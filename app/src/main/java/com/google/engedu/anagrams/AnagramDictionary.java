/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.anagrams;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class AnagramDictionary {

    private static final String TAG = "LogActivity";

    private static final int MIN_NUM_ANAGRAMS = 5;
    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MAX_WORD_LENGTH = 7;
    private Random random = new Random();

    private ArrayList<String> wordList;
    private String[] alphabetList = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
            "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    private int wordLength;

    private HashSet<String> wordSet;
    private HashMap<String, ArrayList<String>> lettersToWord;
    private HashMap<Integer, ArrayList<String>> sizeToWords;

    public AnagramDictionary(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line;
        wordLength = DEFAULT_WORD_LENGTH;
        wordList = new ArrayList<String>(62995);
        wordSet = new HashSet<String>();
        lettersToWord = new HashMap<String, ArrayList<String>>();
        sizeToWords = new HashMap<Integer, ArrayList<String>>();
        while((line = in.readLine()) != null){
            String word = line.trim();
            wordList.add(word);

            int length = word.length();
            if(sizeToWords.containsKey(length))
                sizeToWords.get(length).add(word);
            else {
                ArrayList<String> newList = new ArrayList<String>();
                newList.add(word);
                sizeToWords.put(length, newList);
            }

            if(wordSet.add(word)) {
                String sorted = sortLetters(word);
                if (!lettersToWord.containsKey(sorted)) // if key is not present
                    lettersToWord.put(sorted, new ArrayList<String>());
                lettersToWord.get(sorted).add(word);
            }
        }
    }

    public boolean isGoodWord(String word, String base) {
        Log.i(TAG, "base: " + base + " word: " + word);
        if(wordSet.contains(word) && !word.contains(base)){
            return true;
        } else if(word.contains(" ") && word.length() <= base.length()+2) {
            String a = word.substring(0,word.indexOf(" "));
            String b = word.substring(word.indexOf(" ")+1);
            Log.i(TAG, "A: " + a + " B: " + b);
            if(wordSet.contains(a) && wordSet.contains(b) && !a.contains(base) && !b.contains(base)) {
                String target = sortLetters(a+b);
                String sorted = sortLetters(base);
                int baseIndex = 0;
                boolean mismatch = false;
                for(int i = 0; i < target.length() && baseIndex < sorted.length(); i++) {
                    if(target.charAt(i) == sorted.charAt(baseIndex))
                        baseIndex++;
                    else if(mismatch == true) // only one letter added, so only one mismatch allowed
                        break;
                    else
                        mismatch = true;
                }
                if(baseIndex == sorted.length())
                    return true;
            }
        }
        return false;
    }

    public List<String> getAnagrams(String targetWord) {
        String key = sortLetters(targetWord);
        ArrayList<String> result = lettersToWord.get(key);
        return result;
    }

    public List<String> getAnagramsWithOneMoreLetter(String word) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i = 0; i < alphabetList.length; i++){
            String tempWord = word + alphabetList[i];
            tempWord = sortLetters(tempWord);
            if(lettersToWord.containsKey(tempWord) && lettersToWord.get(tempWord) != null)
                result.addAll(lettersToWord.get(tempWord));
        }
        return result;
    }

    // NEW
    public List<String> getAnagramsWithTwoMoreLetters(String word) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i = 0; i < alphabetList.length; i++){
            for(int j = i; j < alphabetList.length; j++) {
                String tempWord = word + alphabetList[i] + alphabetList[j];
                tempWord = sortLetters(tempWord);
                if (lettersToWord.containsKey(tempWord) && lettersToWord.get(tempWord) != null)
                    result.addAll(lettersToWord.get(tempWord));
            }
        }
        return result;
    }

    public List<String> getAnagramsWithOneOrTwoMoreLetters(String word) {
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(getAnagramsWithOneMoreLetter(word));
        result.addAll(getAnagramsWithTwoMoreLetters(word));
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i).contains(word)) {
                result.remove(i);
                i--;
            }
        }
        return result;
    }

    public String pickGoodStarterWord() {
        while(!sizeToWords.containsKey(wordLength) && wordLength < MAX_WORD_LENGTH)
            wordLength++;
        ArrayList<String> options = sizeToWords.get(wordLength);

        String result = "skate";
        int count = 0;
        //int index = (int)(Math.random()*options.size());
        int index = random.nextInt(options.size());
        for(int i = index; i < options.size(); i++) {
            count++;
            int numAnagrams = 0;
            for(String letterA: alphabetList) { // with one more letter
                String key = sortLetters(options.get(i) + letterA);
                if(lettersToWord.containsKey(key))
                    numAnagrams += lettersToWord.get(key).size();
                for(String letterB: alphabetList) { // with two more letters
                    key = sortLetters(options.get(i) + letterA + letterB);
                    if(lettersToWord.containsKey(key))
                        numAnagrams += lettersToWord.get(key).size();
                }
            }
            if (numAnagrams >= MIN_NUM_ANAGRAMS) {
                result = options.get(i);
                break;
            }
            if (count >= lettersToWord.size()) {
                result = options.get(index);
                break;
            }
            if (i == options.size() - 1)
                i = 0;
        }
        if(wordLength < MAX_WORD_LENGTH)
            wordLength++;
        return result;
    }

    private String sortLetters(String input) {
        char[] chars = input.toCharArray();
        Arrays.sort(chars);
        return (new String(chars));
    }
}
