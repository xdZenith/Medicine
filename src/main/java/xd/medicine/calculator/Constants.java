package xd.medicine.calculator;

/**
 * created by liubotao
 */
public final class Constants {
    private Constants(){
    }

    static final float MTALPHA = (float) 1/12; //计算MT时，权值的随机波动参数
    static final float HBTM = 5; //计算HBT时，抽样的次数
    static final int HBTN = 4; //计算HBT时，每次抽样的样本个数
    static final float DIFFERMAX = (float) 1/2; //计算HBT时，抽样样本值和抽样平均值的最大平均差值
    static final float TRUSTU1 = (float)3/4; //MT的权值为TRUSTU1，HBT的权值为1-TRUSTU1
    static final float TRUSTU2 = (float)1/2; //bsTrust的权值为TRUSTU2，poobtrust的权值为1-TRUSTU2
    static final float THSVALUE = (float)1/2; //计算MT时，当医生的属性满足病人的要求时的阈值
}
