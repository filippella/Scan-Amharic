package org.dalol.scanamharic;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Filippo Engidashet [filippo.eng@gmail.com]
 * @version 1.0.0
 * @since Saturday, 24/03/2018 at 23:18.
 */
public class asjlkasTest {
    @Test
    public void gh_test() throws Exception {
        asjlkas d = new asjlkas();
        String hjhd = d.gh().trim().replaceAll("[\\r\\n\t ]+", "");
        for (int i = 0, length = hjhd.length(); i < length; i++) {
            System.out.print(hjhd.charAt(i));
            if((i +1) %7==0) System.out.print("\t");
//                System.out.print("\t"); else System.out.print(" ");
            if((i +1) %28==0) System.out.println();  else System.out.print(" ");
        }
    }

    @Test
    public void hjhd_test() throws Exception {
        asjlkas d = new asjlkas();
        String hjhd = d.hjhd().trim().replaceAll("[\\r\\n\t ]+", "");
        for (int i = 0, length = hjhd.length(); i < length; i++) {
            System.out.println(hjhd.charAt(i));
        }
    }

}