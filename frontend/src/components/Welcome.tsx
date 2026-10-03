import welcomePic from '../assets/ec-logo.png';
import "./styles/Welcome.css"
import {translatedInfo} from "./utils/TranslatedInfo.ts";

type WelcomeProps = {
    language: string;
}

export default function Welcome(props: Readonly<WelcomeProps>) {
    return (
        <>
            <h1>{translatedInfo["Welcome"][props.language]}</h1>
            <h2>{translatedInfo["to The EC Inventory Hub"][props.language]}</h2>
            <div className="image-wrapper margin-top-20">
                <img
                    src={welcomePic}
                    alt="Welcome to Ec Inventory Hub"
                    className="logo-welcome"
                />
            </div>
        </>
    )
}