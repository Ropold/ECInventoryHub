import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import axios from "axios";
import type {LocationModel} from "../models/LocationModel.ts";
import LocationsForm from "./LocationsForm.tsx";

type EditLocationProps = {
    language: string;
    handleLocationUpdate: (updatedLocation: LocationModel) => void;
}

export default function EditLocation(props: Readonly<EditLocationProps>) {
    const [location, setLocation] = useState<LocationModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();

    const [name, setName] = useState<string>("");
    const [address, setAddress] = useState<string | undefined>(undefined);
    const [phone, setPhone] = useState<string | undefined>(undefined);
    const [email, setEmail] = useState<string | undefined>(undefined);
    const [notes, setNotes] = useState<string | undefined>(undefined);
    const [latitude, setLatitude] = useState<number | undefined>(undefined);
    const [longitude, setLongitude] = useState<number | undefined>(undefined);
    const [image, setImage] = useState<File | null>(null);
    const [imageDeleted, setImageDeleted] = useState<boolean>(false);

    useEffect(() => {
        if (!id) return;
        axios
            .get(`/api/locations/${id}`)
            .then((response) => {
                const data: LocationModel = response.data;
                setLocation(data);
                setName(data.name);
                setAddress(data.address ?? undefined);
                setPhone(data.phone ?? undefined);
                setEmail(data.email ?? undefined);
                setNotes(data.notes ?? undefined);
                setLatitude(data.latitude ?? undefined);
                setLongitude(data.longitude ?? undefined);
            })
            .catch((error) => console.error("Error fetching location details", error));
    }, [id]);

    function handleSaveEdit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (!location) return;

        const updatedLocation = {
            id: location.id,
            name: name,
            address: address ?? null,
            phone: phone ?? null,
            email: email ?? null,
            notes: notes ?? null,
            latitude: latitude ?? null,
            longitude: longitude ?? null,
            imageUrl: imageDeleted ? null : location.imageUrl
        };

        const data = new FormData();
        data.append("locationDTO", new Blob([JSON.stringify(updatedLocation)], {type: "application/json"}));
        if (image) {
            data.append("image", image);
        }

        axios
            .put(`/api/locations/${location.id}`, data, {headers: {"Content-Type": "multipart/form-data"}})
            .then((response) => {
                props.handleLocationUpdate(response.data);
                navigate(`/locations/${location.id}`);
            })
            .catch((error) => console.error("Error updating location", error));
    }

    const backNavigationPath = location?.id ? `/locations/${location.id}` : "/locations";

    return (
        <div>
            <LocationsForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleSaveEdit}
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
                existingImageUrl={location?.imageUrl ?? undefined}
                imageDeleted={imageDeleted}
                setImageDeleted={setImageDeleted}
            />
        </div>
    )
}
