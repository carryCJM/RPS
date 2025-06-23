/**
 * author: Jason
 * 2016��10��25��
 * TODO
 */
package oo.com.iseu.slicer.staticSlicer;

/**
 * @author Jason
 *
 */
public class RandomTest {
	public static void main(String[] args) {
		String strFilePath = "E:/TestCase_final";
		RandomSCGenerator gen = new RandomSCGenerator(strFilePath);
		gen.generate(5);
		gen.startProcess();
	}
}
