import type {LocationModel} from "../models/LocationModel.ts";
import type {DeviceModel} from "../models/DeviceModel.ts";
import MapBoxCard from "./MapBoxCard.tsx";

type LocationsProps = {
    locations: LocationModel[];
    devices: DeviceModel[];
    language: string;
};

export default function Locations(props: Readonly<LocationsProps>) {
    return (
        <>
            <MapBoxCard locations={props.locations} devices={props.devices} language={props.language} />
            <h2>Locations</h2>
        </>
    )
}
