import appleSvg from "./assets/apple-min.jpeg";
import beansSvg from "./assets/beans-min.jpeg";
import blueberriesSvg from "./assets/blueberry-min.jpeg";
import bokChoySvg from "./assets/bok_choy-min.jpeg";
import cabbageSvg from "./assets/cabbage-min.jpeg";
import carrotSvg from "./assets/carrot-min.jpeg";
import cornSvg from "./assets/corn-min.jpeg";
import cottonSvg from "./assets/cotton-min.jpeg";
import onionSvg from "./assets/onion-min.jpeg";
import pepperSvg from "./assets/pepper-min.jpeg";
import potatoSvg from "./assets/potato-min.jpeg";
import pumpkinSvg from "./assets/pumpkin-min.jpeg";
import riceSvg from "./assets/rice-min.jpeg";
import tomatoSvg from "./assets/tomato-min.jpeg";
import wheatSvg from "./assets/wheat-min.jpeg";

export const crops = [
  { name: "Tomato", img: tomatoSvg },
  { name: "Potato", img: potatoSvg },
  { name: "Napa Cabbage", img: cabbageSvg },
  { name: "Rice", img: riceSvg },
  { name: "Wheat", img: wheatSvg },
  { name: "Corn", img: cornSvg },
  { name: "Carrot", img: carrotSvg },
  { name: "Onion", img: onionSvg },
  { name: "Bok Choy", img: bokChoySvg },
  { name: "Cotton", img: cottonSvg },
  { name: "Blueberries", img: blueberriesSvg },
  { name: "Batterfly Beans", img: beansSvg },
  { name: "Spicy Pepper", img: pepperSvg },
  { name: "Rockhopper Pumpkin", img: pumpkinSvg },
  { name: "Apple", img: appleSvg }
] as const;

type Crop = typeof crops[number];

type Props = {
  crop: Crop;
}

export default function ({ crop }: Props) {
  return <img src={crop.img} alt={`${crop.name} icon`} />;
}
