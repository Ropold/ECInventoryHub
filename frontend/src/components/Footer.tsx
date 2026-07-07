import {translatedInfo} from "./utils/TranslatedInfo.ts";

type FooterProps = {
    language: string;
};

export default function Footer(props: Readonly<FooterProps>) {
    return (
        <footer className="footer">
            <p>{translatedInfo["EC Inventory Hub 2026 by R.Stolz"][props.language]}</p>
        </footer>
    )
}