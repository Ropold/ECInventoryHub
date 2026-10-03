import notFoundCover from '../assets/error-404.jpg';
import {translatedInfo} from "./utils/TranslatedInfo.ts";

type NotFoundProps = {
    language: string;
}

export default function NotFound(props: Readonly<NotFoundProps>) {
    return (
        <div>
            <h2>{translatedInfo["Not found message"][props.language]}</h2>
            <img src={notFoundCover} alt="404 Lego" style={{ width: '500px'}}/>
        </div>
    )
}