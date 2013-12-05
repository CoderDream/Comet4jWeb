package org.comet4j.demo.helloworld;

public class RandomDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			int ran = (int) (Math.random() * 1000); // 另一种方式 java.lang.Math
			System.out.println(ran);
		}

	}

}
