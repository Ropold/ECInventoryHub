import type {DeviceModel} from "../models/DeviceModel.ts";

type AddNewDeviceProps = {
    language: string;
    role: string;
    handleNewDeviceSubmit: (newDevice: DeviceModel) => void;
}

export default function AddNewDevice(props: Readonly<AddNewDeviceProps>) {
    return (
        <>
            <h2>Add New Device</h2>
            <p>{props.language}</p>
        </>
    )
}
