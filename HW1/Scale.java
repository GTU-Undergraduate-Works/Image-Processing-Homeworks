

import vpt.*;
import vpt.algorithms.display.Display2D;
import vpt.algorithms.io.Load;

public class Scale {

    public static void main(String[]  args) {

        Image input = Load.invoke("images/valve.png");
        Image output = null;
        try {
            output = scale(input, 2.0, 2.0, 0.5235987756 );
        } catch (GlobalException e) {
            System.out.println(e.getMessage());
        }
        Display2D.invoke(input, "Input Image");
        Display2D.invoke(output, "Scaled Image");
    }

    public static Image scale(Image input, double scaleX, double scaleY, double rotAngle) throws GlobalException {

        if (scaleX < 1.0 || scaleY < 1.0)
            throw new GlobalException("Scale factors must be greater than 1");
        Image interpolatedImage = bilenearInterpolation(input, scaleX, scaleY);
        return rotateCounterClockwise(interpolatedImage, rotAngle);
    }

    private static Image bilenearInterpolation(Image input, double scaleX, double scaleY) {

        int width = input.getXDim();
        int height = input.getYDim();
        int newWidth = (int) (width*scaleX);
        int newHeight = (int) (height*scaleY);
        Image output = new ByteImage(newWidth, newHeight);
        int x1, y1, x2, y2;
        int Q11, Q12, Q21, Q22;
        double a, b, p;

        for (int y = 0; y < newHeight; ++y) {
            for (int x = 0; x < newWidth; ++x) {

                    x1 = (int) Math.floor(x /scaleX);
                    x2 = (int) Math.ceil(x/scaleX);
                    y1 = (int) Math.floor(y/scaleY);
                    y2 = (int) Math.ceil(y/scaleY);
                    if (x2 >= width)
                        x2 = width-1;
                    if (y2 >= height)
                        y2 = height-1;
                    Q11 = input.getXYByte(x1,y1);
                    Q12 = input.getXYByte(x1,y2);
                    Q21 = input.getXYByte(x2, y1);
                    Q22 = input.getXYByte(x2,y2);
                    a = (double)y/scaleY - Math.floor((double)y/scaleY);
                    b = (double)x/scaleX - Math.floor((double)x/scaleX);
                    p = (1-a)*(1-b)*Q11 + (1-a)*b*Q12 +a*(1-b)*Q21 + a*b*Q22;
                    output.setXYByte(x,y,(int) p);
            }
        }
        return output;
    }

    private static Image rotateCounterClockwise(Image input, double angle) {

        int width = input.getXDim();
        int height = input.getYDim();
        Image rotatedImage = new ByteImage(width, height);
        double sinus = Math.sin(angle);
        double cosinus = Math.cos(angle);
        double centerX = 0.5*(width-1);
        double centerY = 0.5*(height-1);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                double distanceX = x - centerX;
                double distanceY = y - centerY;
                int newPositionX = (int) (+distanceX * sinus + distanceY * cosinus + centerX);
                int newPositionY = (int) (+distanceX * cosinus - distanceY * sinus + centerY);
                if (newPositionX >= 0 && newPositionX < width && newPositionY >= 0 && newPositionY < height)
                    rotatedImage.setXYByte(x, y, input.getXYByte(newPositionX, newPositionY));
            }
        }
        return rotatedImage;
    }
}
