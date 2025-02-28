package com.sucheon.alarm.utils;

/**
 * KMP字符串部分匹配表
 */
public class KMPUtils {
    public static int[] computeLPSArray(String pat) {
        int M = pat.length();
        int len = 0;
        int lps[] = new int[M];
        int i = 1;
        lps[0] = 0; // lps[0] is always 0

        // 构建部分匹配表
        while (i < M) {
            if (pat.charAt(i) == pat.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                // 当失配时，len后退到之前最长的相同前后缀的长度
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = len;
                    i++;
                }
            }
        }
        return lps;
    }

    public static int KMPSearch(String pat, String txt) {
        int M = pat.length();
        int N = txt.length();
        int lps[] = computeLPSArray(pat);
        int i = 0;
        int j = 0;
        while (i < N) {
            if (pat.charAt(j) == txt.charAt(i)) {
                j++;
                i++;
            }
            if (j == M) {
                return i - M;
            }
            // mismatch after j matches
            else if (i < N && pat.charAt(j) != txt.charAt(i)) {
                // Do not match lps[0..lps[j-1]] characters,
                // they will match anyway
                if (j != 0)
                    j = lps[j - 1];
                else
                    i = i + 1;
            }
        }
        return -1;
    }

    public static void main(String args[]) {
        String txt = "oc_key_field(\"tevdfg_xxxx_ddff\", \"rms\", \"yyyy\") > 10 || oc_key_field(\"tevdfg_xxxx_dd44\", \"rms\", \"yyyy\") > 12 && oc_key_field(\"tevdfg_xxxx_ddff\", \"rms\", \"yyyy\") > 10 || oc_key_field(\"tevdfg_xxxx_dd44\", \"rms\", \"yyyy\") > 45";
        String pat = "yyyy";
        int res = KMPSearch(pat, txt);
        if (res == -1)
            System.out.println("Not found");
        else
            System.out.println("Found at index " + res);

        String result = txt.substring(res, res+pat.length());
        System.out.println(result);
    }
}
