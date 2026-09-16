import type {DeviceModel} from "../models/DeviceModel.ts";

type DeviceDetailsProps = {
    language: string;
    role: string;
    handleDeviceUpdate: (updatedDevice: DeviceModel) => void;
    handleDeviceDelete: (deletedDeviceId: string) => void;
}

export default function DeviceDetails(props: Readonly<DeviceDetailsProps>) {
    return (
        <>
            <h2>Device Details</h2>
            <p>{props.language}</p>
        </>
    )
}
