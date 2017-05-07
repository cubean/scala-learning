

package object scalashop {

  /** The value of every pixel is represented as a 32 bit integer. */
  type RGBA = Int

  /** Returns the red component. */
  def red(c: RGBA): Int = (0xff000000 & c) >>> 24

  /** Returns the green component. */
  def green(c: RGBA): Int = (0x00ff0000 & c) >>> 16

  /** Returns the blue component. */
  def blue(c: RGBA): Int = (0x0000ff00 & c) >>> 8

  /** Returns the alpha component. */
  def alpha(c: RGBA): Int = (0x000000ff & c) >>> 0

  /** Used to create an RGBA value from separate components. */
  def rgba(r: Int, g: Int, b: Int, a: Int): RGBA = {
    (r << 24) | (g << 16) | (b << 8) | (a << 0)
  }

  /** Restricts the integer into the specified range. */
  def clamp(v: Int, min: Int, max: Int): Int = {
    if (v < min) min
    else if (v > max) max
    else v
  }

  /** Image is a two-dimensional matrix of pixel values. */
  class Img(val width: Int, val height: Int, private val data: Array[RGBA]) {
    def this(w: Int, h: Int) = this(w, h, new Array(w * h))

    def apply(x: Int, y: Int): RGBA = data(y * width + x)

    def update(x: Int, y: Int, c: RGBA): Unit = data(y * width + x) = c
  }

  /** Computes the blurred RGBA value of a single pixel of the input image. */
  def boxBlurKernel(src: Img, x: Int, y: Int, radius: Int): RGBA = {
    // TODO implement using while loops

    if (radius > 0) {
      val p1 = (clamp(x - radius, 0, src.width - 1), clamp(y - radius, 0, src.height - 1))
      val p2 = (clamp(x - radius, 0, src.width - 1), clamp(y + radius, 0, src.height - 1))
      val p3 = (clamp(x + radius, 0, src.width - 1), clamp(y + radius, 0, src.height - 1))
      val p4 = (clamp(x + radius, 0, src.width - 1), clamp(y - radius, 0, src.height - 1))

      val l1 = p1._2 until p2._2
      val l2 = p2._1 until p3._1
      val l3 = (p3._2 until p4._2).by(-1)
      val l4 = (p4._1 until p1._1).by(-1)

      def calRGBA(func: RGBA => Int): Int =
        ((if (l1.nonEmpty) l1.map(c => func(src(p1._1, c))).sum else 0) +
          (if (l2.nonEmpty) l2.map(c => func(src(c, p2._2))).sum else 0) +
          (if (l3.nonEmpty) l3.map(c => func(src(p3._1, c))).sum else 0) +
          (if (l4.nonEmpty) l4.map(c => func(src(c, p4._2))).sum else 0)) /
          (l1.length + l2.length + l3.length + l4.length)


      val rgbaValue = common.parallel(calRGBA(red), calRGBA(green), calRGBA(blue), calRGBA(alpha))
      rgba(rgbaValue._1, rgbaValue._2, rgbaValue._3, rgbaValue._4)
    }
    else {
      src(x, y)
    }
  }

}
