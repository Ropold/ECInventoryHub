import {useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import type {LocationModel} from "../models/LocationModel.ts";
import LocationsForm from "./LocationsForm.tsx";
import {resolveCoordinates} from "../utils/MapboxGeocoding.ts";

type AddNewLocationProps = {
    language: string;
    role: string;
    handleNewLocationSubmit: (newLocation: LocationModel) => void;
}

export default function AddNewLocation(props: Readonly<AddNewLocationProps>) {

    const [name, setName] = useState<string>("");
    const [address, setAddress] = useState<string | undefined>(undefined);
    const [phone, setPhone] = useState<string | undefined>(undefined);
    const [email, setEmail] = useState<string | undefined>(undefined);
    const [notes, setNotes] = useState<string | undefined>(undefined);
    const [latitude, setLatitude] = useState<number | undefined>(undefined);
    const [longitude, setLongitude] = useState<number | undefined>(undefined);
    const [image, setImage] = useState<File | null>(null);
    const [imageDeleted, setImageDeleted] = useState<boolean>(false);

    const navigate = useNavigate();

    function handleNewAddSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();

        // Fehlende Koordinaten einmalig aus der Adresse holen
        resolveCoordinates(address, {latitude: latitude ?? null, longitude: longitude ?? null}, false)
            .then((coordinates) => {
                const newLocation = {
                    id: null,
                    name: name,
                    address: address ?? null,
                    phone: phone ?? null,
                    email: email ?? null,
                    notes: notes ?? null,
                    latitude: coordinates.latitude,
                    longitude: coordinates.longitude,
                    imageUrl: null
                };

                const data = new FormData();
                data.append("locationDTO", new Blob([JSON.stringify(newLocation)], {type: "application/json"}));
                if (image) {
                    data.append("image", image);
                }

                return axios.post('/api/locations', data, {headers: {"Content-Type": "multipart/form-data"}});
            })
            .then((response) => {
                props.handleNewLocationSubmit(response.data);
                navigate(`/locations/${response.data.id}`);
            })
            .catch((error) => console.error("Error creating location", error));
    }

    const backNavigationPath = "/locations";

    return (
        <div>
            <LocationsForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleNewAddSubmit}
                name={name}
                setName={setName}
                address={address}
                setAddress={setAddress}
                phone={phone}
                setPhone={setPhone}
                email={email}
                setEmail={setEmail}
                notes={notes}
                setNotes={setNotes}
                latitude={latitude}
                setLatitude={setLatitude}
                longitude={longitude}
                setLongitude={setLongitude}
                image={image}
                setImage={setImage}
                imageDeleted={imageDeleted}
                setImageDeleted={setImageDeleted}
            />
        </div>
    )
}
