import type {LocationModel} from "../models/LocationModel.ts";
import {useNavigate} from "react-router-dom";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import "../styles/location/LocationCard.css";
import "../styles/CardBadge.css";

type LocationCardProps = {
    location: LocationModel;
    language: string;
}

// 6 Nachkommastellen entsprechen ca. 10 cm – genauer braucht es auf der Karte nicht
function formatCoordinate(value: number | null | undefined): string {
    return value == null ? "—" : value.toFixed(6);
}

export default function LocationCard(props: Readonly<LocationCardProps>){
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/locations/${props.location.id}`);
    }

    return (
        <button type="button" className="location-card" onClick={handleCardClick}>
            <h2 className="card-title">{props.location.name}</h2>
            <p className="card-subtitle">{props.location.address ?? "—"}</p>
            <p className="card-detail">
                {translatedInfo["Latitude"][props.language]}: {formatCoordinate(props.location.latitude)}
            </p>
            <p className="card-detail">
                {translatedInfo["Longitude"][props.language]}: {formatCoordinate(props.location.longitude)}
            </p>
            {props.location.imageUrl && (
                <img
                    className="location-card-image"
                    src={props.location.imageUrl}
                    alt={props.location.name}
                />
            )}
        </button>
    )
}
