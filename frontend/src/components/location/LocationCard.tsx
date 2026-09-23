import type {LocationModel} from "../models/LocationModel.ts";
import {useNavigate} from "react-router-dom";
import "../styles/location/LocationCard.css";

type LocationCardProps = {
    location: LocationModel;
    language: string;
}

export default function LocationCard(props: Readonly<LocationCardProps>){
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/locations/${props.location.id}`);
    }

    return (
        <button type="button" className="location-card" onClick={handleCardClick}>
            <h2>{props.location.name}</h2>
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
